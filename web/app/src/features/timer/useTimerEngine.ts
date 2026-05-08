import { useCallback, useEffect, useRef, useState } from "react";
import { getSettingsSnapshot } from "@/data/settings";
import { playCue } from "@/lib/audio";
import { buzzForCue } from "@/lib/haptics";
import {
  TIMER_TICK_MS,
  TimerConfig,
  TimerEngine,
  TimerState,
} from "@/lib/timer";

export function useTimerEngine() {
  const engineRef = useRef<TimerEngine | null>(null);
  if (!engineRef.current) engineRef.current = new TimerEngine();
  const engine = engineRef.current;

  const [state, setState] = useState<TimerState>(() => engine.getState());

  useEffect(() => {
    return engine.subscribe(setState);
  }, [engine]);

  // Cue dispatch — gated by user settings, read at fire time so toggles take
  // effect immediately without re-subscribing.
  useEffect(() => {
    return engine.onCue((cue) => {
      const s = getSettingsSnapshot();
      if (s.soundEnabled) playCue(cue);
      if (s.hapticsEnabled) buzzForCue(cue);
    });
  }, [engine]);

  useEffect(() => {
    if (!state.isRunning) return;
    const id = window.setInterval(() => engine.tick(Date.now()), TIMER_TICK_MS);
    return () => window.clearInterval(id);
  }, [engine, state.isRunning]);

  const start = useCallback(
    (config: TimerConfig) => engine.start(config, Date.now()),
    [engine],
  );
  const pause = useCallback(() => engine.pause(Date.now()), [engine]);
  const resume = useCallback(() => engine.resume(Date.now()), [engine]);
  const reset = useCallback(() => engine.reset(), [engine]);
  const skip = useCallback(() => engine.skip(Date.now()), [engine]);

  return { state, start, pause, resume, reset, skip, engine };
}
