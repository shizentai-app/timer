import { useEffect, useState } from "react";

export interface TimerSettings {
  soundEnabled: boolean;
  hapticsEnabled: boolean;
  keepScreenOn: boolean;
}

const DEFAULTS: TimerSettings = {
  soundEnabled: true,
  hapticsEnabled: true,
  keepScreenOn: true,
};

const STORAGE_KEY = "shizentai-timer.settings";

function read(): TimerSettings {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return DEFAULTS;
    const parsed = JSON.parse(raw) as Partial<TimerSettings>;
    return { ...DEFAULTS, ...parsed };
  } catch {
    return DEFAULTS;
  }
}

function write(s: TimerSettings): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
  } catch {
    // ignore
  }
}

let current: TimerSettings = read();
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((l) => l());
}

export function getSettingsSnapshot(): TimerSettings {
  return current;
}

export interface SettingsApi {
  settings: TimerSettings;
  setSound(on: boolean): void;
  setHaptics(on: boolean): void;
  setKeepScreenOn(on: boolean): void;
}

export function useSettings(): SettingsApi {
  const [, force] = useState(0);
  useEffect(() => {
    const l = () => force((n) => n + 1);
    listeners.add(l);
    return () => {
      listeners.delete(l);
    };
  }, []);
  return {
    settings: current,
    setSound(on: boolean) {
      current = { ...current, soundEnabled: on };
      write(current);
      notify();
    },
    setHaptics(on: boolean) {
      current = { ...current, hapticsEnabled: on };
      write(current);
      notify();
    },
    setKeepScreenOn(on: boolean) {
      current = { ...current, keepScreenOn: on };
      write(current);
      notify();
    },
  };
}
