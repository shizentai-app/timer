import { describe, expect, it } from "vitest";
import {
  AudioCue,
  REST_END_CUE_MS,
  TimerConfig,
  TimerEngine,
  TimerState,
  buildSequence,
  formatMmSs,
} from "./timer";

const KUMITE_30: TimerConfig = {
  workSeconds: 30,
  restSeconds: 30,
  rounds: 3,
  prepSeconds: 10,
  warningSeconds: 10,
  signalEndOfRest: true,
};

const TABATA: TimerConfig = {
  workSeconds: 20,
  restSeconds: 10,
  rounds: 8,
  prepSeconds: 10,
  warningSeconds: 5,
  signalEndOfRest: false,
};

function harness(engine: TimerEngine) {
  const states: TimerState[] = [];
  const cues: AudioCue[] = [];
  engine.subscribe((s) => states.push({ ...s }));
  engine.onCue((c) => cues.push(c));
  return { states, cues };
}

describe("buildSequence", () => {
  it("includes PREPARE when prepSeconds > 0", () => {
    const seq = buildSequence(KUMITE_30);
    expect(seq[0]).toMatchObject({ phase: "PREPARE", durationMs: 10_000, round: 0 });
  });

  it("skips PREPARE when prepSeconds = 0", () => {
    const seq = buildSequence({ ...KUMITE_30, prepSeconds: 0 });
    expect(seq[0]).toMatchObject({ phase: "WORK", round: 1 });
  });

  it("alternates WORK/REST and skips REST after the last round", () => {
    const seq = buildSequence(KUMITE_30);
    // 1 PREPARE + 3 WORK + 2 REST = 6 (no REST after round 3)
    expect(seq).toHaveLength(6);
    expect(seq[1]).toMatchObject({ phase: "WORK", round: 1 });
    expect(seq[2]).toMatchObject({ phase: "REST", round: 1 });
    expect(seq[3]).toMatchObject({ phase: "WORK", round: 2 });
    expect(seq[4]).toMatchObject({ phase: "REST", round: 2 });
    expect(seq[5]).toMatchObject({ phase: "WORK", round: 3 });
  });

  it("omits REST when restSeconds = 0", () => {
    const seq = buildSequence({ ...KUMITE_30, restSeconds: 0 });
    expect(seq.filter((s) => s.phase === "REST")).toHaveLength(0);
  });
});

describe("TimerEngine — start", () => {
  it("emits initial state with phase=PREPARE and round=0", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start(KUMITE_30, 0);

    expect(states.at(-1)).toMatchObject({
      phase: "PREPARE",
      isRunning: true,
      round: 0,
      totalRounds: 3,
      remainingMs: 10_000,
      totalMs: 10_000,
    });
    expect(cues).toEqual([]); // PREPARE has no cue
  });

  it("emits work_start when prep is skipped", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start({ ...KUMITE_30, prepSeconds: 0 }, 0);

    expect(states.at(-1)?.phase).toBe("WORK");
    expect(cues).toEqual(["work_start"]);
  });
});

describe("TimerEngine — tick & phase transitions", () => {
  it("decrements remainingMs on tick", () => {
    const engine = new TimerEngine();
    const { states } = harness(engine);
    engine.start(KUMITE_30, 0);
    engine.tick(2_500);
    expect(states.at(-1)?.remainingMs).toBe(7_500);
  });

  it("transitions PREPARE → WORK and emits work_start", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start(KUMITE_30, 0);
    engine.tick(10_000); // PREPARE done
    expect(states.at(-1)).toMatchObject({
      phase: "WORK",
      round: 1,
      remainingMs: 30_000,
      totalMs: 30_000,
    });
    expect(cues).toContain("work_start");
  });

  it("enters WARNING phase in the last warningSeconds and emits work_warning once", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start(KUMITE_30, 0);
    engine.tick(10_000); // start of WORK round 1
    // 30s WORK with warningSeconds=10 → WARNING when remaining ≤ 10000ms
    engine.tick(30_000); // 20s into WORK, remaining=10s — exactly at boundary
    expect(states.at(-1)?.phase).toBe("WARNING");
    expect(cues.filter((c) => c === "work_warning")).toHaveLength(1);
    // Subsequent ticks within WARNING should not re-fire the cue
    engine.tick(35_000);
    engine.tick(38_000);
    expect(cues.filter((c) => c === "work_warning")).toHaveLength(1);
  });

  it("transitions WORK/WARNING → REST and emits rest_start", () => {
    const engine = new TimerEngine();
    const { cues } = harness(engine);
    engine.start(KUMITE_30, 0);
    engine.tick(10_000); // → WORK r1
    engine.tick(40_000); // WORK done → REST r1
    expect(cues.filter((c) => c === "rest_start")).toHaveLength(1);
  });

  it("emits rest_ending exactly once when remaining ≤ 10s in REST and signalEndOfRest=true", () => {
    const engine = new TimerEngine();
    const { cues } = harness(engine);
    engine.start(KUMITE_30, 0);
    engine.tick(10_000); // → WORK r1
    engine.tick(40_000); // → REST r1 (30s)
    // REST started at t=40_000. rest_ending fires when remaining ≤ 10_000.
    // Remaining ≤ 10_000 when phaseElapsed ≥ 20_000 → t ≥ 60_000.
    engine.tick(55_000); // remaining 15_000 — too early
    expect(cues.filter((c) => c === "rest_ending")).toHaveLength(0);
    engine.tick(60_500); // remaining ~9_500 — fires
    expect(cues.filter((c) => c === "rest_ending")).toHaveLength(1);
    // Subsequent ticks within REST should not re-fire
    engine.tick(65_000);
    expect(cues.filter((c) => c === "rest_ending")).toHaveLength(1);
  });

  it("does NOT emit rest_ending when signalEndOfRest=false", () => {
    const engine = new TimerEngine();
    const { cues } = harness(engine);
    engine.start(TABATA, 0);
    engine.tick(10_000); // → WORK r1 (20s)
    engine.tick(30_000); // → REST r1 (10s)
    engine.tick(31_000); // remaining 9s, would normally fire
    expect(cues).not.toContain("rest_ending");
  });

  it("does NOT enter WARNING when warningSeconds = 0", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start({ ...KUMITE_30, warningSeconds: 0 }, 0);
    engine.tick(10_000); // → WORK r1
    engine.tick(35_000); // 25s into WORK
    expect(states.at(-1)?.phase).toBe("WORK");
    expect(cues).not.toContain("work_warning");
  });

  it("transitions to FINISHED after the last WORK and emits finished", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start({ workSeconds: 10, restSeconds: 5, rounds: 2, prepSeconds: 0, warningSeconds: 0, signalEndOfRest: false }, 0);
    engine.tick(10_000); // WORK r1 → REST r1
    engine.tick(15_000); // REST done → WORK r2
    engine.tick(25_000); // WORK r2 done → FINISHED
    expect(states.at(-1)).toMatchObject({ phase: "FINISHED", isRunning: false, remainingMs: 0 });
    expect(cues.at(-1)).toBe("finished");
  });
});

describe("TimerEngine — pause / resume", () => {
  it("preserves remaining ms across pause/resume", () => {
    const engine = new TimerEngine();
    const { states } = harness(engine);
    engine.start(KUMITE_30, 0);
    engine.tick(3_000); // remaining 7_000
    engine.pause(3_000);
    expect(states.at(-1)?.isRunning).toBe(false);
    expect(states.at(-1)?.remainingMs).toBe(7_000);

    // 5s real-time passes while paused
    engine.resume(8_000);
    engine.tick(9_000); // 1s after resume → another 1s consumed → remaining 6_000
    expect(states.at(-1)?.remainingMs).toBe(6_000);
  });

  it("ignores pause when not running", () => {
    const engine = new TimerEngine();
    const before = engine.getState();
    engine.pause(0);
    expect(engine.getState()).toEqual(before);
  });

  it("ignores resume when not paused or in IDLE/FINISHED", () => {
    const engine = new TimerEngine();
    engine.resume(0);
    expect(engine.getState().isRunning).toBe(false);
  });
});

describe("TimerEngine — reset", () => {
  it("returns to INITIAL_STATE", () => {
    const engine = new TimerEngine();
    engine.start(KUMITE_30, 0);
    engine.tick(5_000);
    engine.reset();
    expect(engine.getState()).toMatchObject({
      phase: "IDLE",
      isRunning: false,
      round: 0,
      totalRounds: 0,
      remainingMs: 0,
      totalMs: 0,
    });
  });
});

describe("TimerEngine — skip", () => {
  it("advances to the next sequence step", () => {
    const engine = new TimerEngine();
    const { states, cues } = harness(engine);
    engine.start(KUMITE_30, 0);
    expect(states.at(-1)?.phase).toBe("PREPARE");
    engine.skip(1_000);
    expect(states.at(-1)?.phase).toBe("WORK");
    expect(cues).toContain("work_start");
  });
});

describe("TimerEngine — REST_END_CUE_MS sanity", () => {
  it("is exactly 10 seconds", () => {
    expect(REST_END_CUE_MS).toBe(10_000);
  });
});

describe("formatMmSs", () => {
  it("formats whole seconds", () => {
    expect(formatMmSs(30_000)).toBe("00:30");
    expect(formatMmSs(60_000)).toBe("01:00");
    expect(formatMmSs(125_000)).toBe("02:05");
  });

  it("rounds up partial seconds (so a tick at 29.5s reads as 00:30)", () => {
    expect(formatMmSs(29_500)).toBe("00:30");
    expect(formatMmSs(1)).toBe("00:01");
    expect(formatMmSs(0)).toBe("00:00");
  });
});
