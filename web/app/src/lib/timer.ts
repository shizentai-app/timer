/**
 * Pure interval-timer engine. No DOM, no React, no setInterval — the consumer
 * drives ticks via tick(now). This makes it deterministic and synchronously
 * testable. Mirrors the Kotlin engine in android/.../engine/TimerEngine.kt.
 */

export type Phase =
  | "IDLE"
  | "PREPARE"
  | "WORK"
  | "WARNING"
  | "REST"
  | "FINISHED";

export type AudioCue =
  | "work_start" // gong_twice
  | "work_warning" // alert
  | "rest_start" // gong
  | "rest_ending" // rest_end (10s before rest ends)
  | "finished"; // gong

export interface TimerConfig {
  workSeconds: number;
  restSeconds: number;
  rounds: number;
  prepSeconds: number;
  warningSeconds: number;
  signalEndOfRest: boolean;
}

export interface TimerState {
  phase: Phase;
  isRunning: boolean;
  round: number; // 1-indexed once WORK starts; 0 during IDLE/PREPARE/FINISHED
  totalRounds: number;
  remainingMs: number;
  totalMs: number; // duration of the current sequence step
}

export const TIMER_TICK_MS = 100;
export const REST_END_CUE_MS = 10_000;

const INITIAL_STATE: TimerState = {
  phase: "IDLE",
  isRunning: false,
  round: 0,
  totalRounds: 0,
  remainingMs: 0,
  totalMs: 0,
};

type PhaseStep = {
  phase: "PREPARE" | "WORK" | "REST";
  durationMs: number;
  round: number;
};

export function buildSequence(config: TimerConfig): PhaseStep[] {
  const seq: PhaseStep[] = [];
  if (config.prepSeconds > 0) {
    seq.push({ phase: "PREPARE", durationMs: config.prepSeconds * 1000, round: 0 });
  }
  for (let r = 1; r <= config.rounds; r++) {
    seq.push({ phase: "WORK", durationMs: config.workSeconds * 1000, round: r });
    if (r < config.rounds && config.restSeconds > 0) {
      seq.push({ phase: "REST", durationMs: config.restSeconds * 1000, round: r });
    }
  }
  return seq;
}

type StateListener = (state: TimerState) => void;
type CueListener = (cue: AudioCue) => void;

export class TimerEngine {
  private state: TimerState = INITIAL_STATE;
  private sequence: PhaseStep[] = [];
  private seqIndex = 0;
  private phaseStartedAt = 0;
  private elapsedAtPause = 0;
  private restEndCueFired = false;
  private warningEntered = false;
  private config: TimerConfig | null = null;
  private stateListeners = new Set<StateListener>();
  private cueListeners = new Set<CueListener>();

  getState(): TimerState {
    return this.state;
  }

  subscribe(listener: StateListener): () => void {
    this.stateListeners.add(listener);
    return () => {
      this.stateListeners.delete(listener);
    };
  }

  onCue(listener: CueListener): () => void {
    this.cueListeners.add(listener);
    return () => {
      this.cueListeners.delete(listener);
    };
  }

  start(config: TimerConfig, now: number): void {
    this.config = config;
    this.sequence = buildSequence(config);
    this.seqIndex = 0;
    this.phaseStartedAt = now;
    this.elapsedAtPause = 0;
    this.restEndCueFired = false;
    this.warningEntered = false;

    const first = this.sequence[0];
    if (!first) {
      this.emitState({
        ...INITIAL_STATE,
        totalRounds: config.rounds,
        phase: "FINISHED",
      });
      this.emitCue("finished");
      return;
    }

    this.emitState({
      phase: first.phase,
      isRunning: true,
      round: first.round,
      totalRounds: config.rounds,
      remainingMs: first.durationMs,
      totalMs: first.durationMs,
    });
    if (first.phase === "WORK") this.emitCue("work_start");
    else if (first.phase === "REST") this.emitCue("rest_start");
  }

  pause(now: number): void {
    if (!this.state.isRunning) return;
    this.elapsedAtPause += now - this.phaseStartedAt;
    this.emitState({ ...this.state, isRunning: false });
  }

  resume(now: number): void {
    if (this.state.isRunning) return;
    if (this.state.phase === "IDLE" || this.state.phase === "FINISHED") return;
    this.phaseStartedAt = now;
    this.emitState({ ...this.state, isRunning: true });
  }

  reset(): void {
    this.sequence = [];
    this.seqIndex = 0;
    this.phaseStartedAt = 0;
    this.elapsedAtPause = 0;
    this.restEndCueFired = false;
    this.warningEntered = false;
    this.config = null;
    this.emitState(INITIAL_STATE);
  }

  skip(now: number): void {
    if (!this.config) return;
    this.advanceToPhase(this.seqIndex + 1, now);
  }

  tick(now: number): void {
    if (!this.state.isRunning || !this.config) return;
    const step = this.sequence[this.seqIndex];
    if (!step) return;

    const phaseElapsed = this.elapsedAtPause + (now - this.phaseStartedAt);
    const remaining = Math.max(0, step.durationMs - phaseElapsed);

    if (remaining <= 0) {
      this.advanceToPhase(this.seqIndex + 1, now);
      return;
    }

    let displayPhase: Phase = step.phase;
    let warningJustEntered = false;
    if (step.phase === "WORK" && this.config.warningSeconds > 0) {
      if (remaining <= this.config.warningSeconds * 1000) {
        displayPhase = "WARNING";
        if (!this.warningEntered) {
          warningJustEntered = true;
          this.warningEntered = true;
        }
      }
    }

    let restEndingJustFired = false;
    if (
      step.phase === "REST" &&
      this.config.signalEndOfRest &&
      !this.restEndCueFired &&
      remaining <= REST_END_CUE_MS
    ) {
      restEndingJustFired = true;
      this.restEndCueFired = true;
    }

    this.emitState({
      phase: displayPhase,
      isRunning: true,
      round: step.round || this.state.round,
      totalRounds: this.state.totalRounds,
      remainingMs: remaining,
      totalMs: step.durationMs,
    });
    if (warningJustEntered) this.emitCue("work_warning");
    if (restEndingJustFired) this.emitCue("rest_ending");
  }

  private advanceToPhase(nextIdx: number, now: number): void {
    if (nextIdx >= this.sequence.length) {
      this.emitState({
        ...this.state,
        phase: "FINISHED",
        isRunning: false,
        remainingMs: 0,
        totalMs: 0,
      });
      this.emitCue("finished");
      return;
    }

    this.seqIndex = nextIdx;
    this.phaseStartedAt = now;
    this.elapsedAtPause = 0;
    this.restEndCueFired = false;
    this.warningEntered = false;

    const next = this.sequence[nextIdx];
    this.emitState({
      phase: next.phase,
      isRunning: true,
      round: next.round || this.state.round,
      totalRounds: this.state.totalRounds,
      remainingMs: next.durationMs,
      totalMs: next.durationMs,
    });
    if (next.phase === "WORK") this.emitCue("work_start");
    else if (next.phase === "REST") this.emitCue("rest_start");
  }

  private emitState(next: TimerState): void {
    this.state = next;
    this.stateListeners.forEach((l) => l(next));
  }

  private emitCue(cue: AudioCue): void {
    this.cueListeners.forEach((l) => l(cue));
  }
}

export function formatMmSs(ms: number): string {
  const totalSec = Math.ceil(ms / 1000);
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}
