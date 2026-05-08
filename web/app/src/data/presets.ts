import { useEffect, useState } from "react";
import { TimerConfig } from "@/lib/timer";

export interface Preset {
  id: string;
  name: string;
  config: TimerConfig;
  isBuiltIn: boolean;
}

export const BUILT_IN_PRESETS: Preset[] = [
  {
    id: "kumite-30s",
    name: "Kumite 30 sec",
    config: { workSeconds: 30, restSeconds: 30, rounds: 3, prepSeconds: 10, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "kumite-1",
    name: "Kumite 1 min",
    config: { workSeconds: 60, restSeconds: 30, rounds: 3, prepSeconds: 10, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "kumite-2",
    name: "Kumite 2 min",
    config: { workSeconds: 120, restSeconds: 60, rounds: 3, prepSeconds: 10, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "kumite-3",
    name: "Kumite 3 min",
    config: { workSeconds: 180, restSeconds: 60, rounds: 3, prepSeconds: 10, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "classic-boxing",
    name: "Classic boxing",
    config: { workSeconds: 180, restSeconds: 60, rounds: 12, prepSeconds: 5, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "amateur-boxing",
    name: "Amateur boxing",
    config: { workSeconds: 120, restSeconds: 60, rounds: 4, prepSeconds: 5, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "mma",
    name: "MMA",
    config: { workSeconds: 300, restSeconds: 60, rounds: 5, prepSeconds: 5, warningSeconds: 10, signalEndOfRest: true },
    isBuiltIn: true,
  },
  {
    id: "tabata",
    name: "Tabata",
    config: { workSeconds: 20, restSeconds: 10, rounds: 8, prepSeconds: 10, warningSeconds: 5, signalEndOfRest: false },
    isBuiltIn: true,
  },
];

export const DEFAULT_PRESET = BUILT_IN_PRESETS[0]!;

const STORAGE_CUSTOMS = "shizentai-timer.presets.custom";
const STORAGE_ACTIVE = "shizentai-timer.presets.active";

function readCustoms(): Preset[] {
  try {
    const raw = localStorage.getItem(STORAGE_CUSTOMS);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as Preset[];
    return Array.isArray(parsed) ? parsed.filter((p) => p && !p.isBuiltIn) : [];
  } catch {
    return [];
  }
}

function writeCustoms(customs: Preset[]): void {
  try {
    localStorage.setItem(STORAGE_CUSTOMS, JSON.stringify(customs));
  } catch {
    // storage full / disabled — ignore
  }
}

function readActiveId(): string {
  try {
    return localStorage.getItem(STORAGE_ACTIVE) || DEFAULT_PRESET.id;
  } catch {
    return DEFAULT_PRESET.id;
  }
}

function writeActiveId(id: string): void {
  try {
    localStorage.setItem(STORAGE_ACTIVE, id);
  } catch {
    // ignore
  }
}

// In-memory cache + pub/sub so TimerScreen + PresetsScreen stay in sync without
// a Context Provider above the routes.
let customs: Preset[] = readCustoms();
let activeId: string = readActiveId();
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((l) => l());
}

export function newCustomId(): string {
  return `custom-${Date.now()}`;
}

function getAll(): Preset[] {
  return [...BUILT_IN_PRESETS, ...customs];
}

function getActivePreset(): Preset {
  return getAll().find((p) => p.id === activeId) ?? DEFAULT_PRESET;
}

export interface PresetsApi {
  all: Preset[];
  activeId: string;
  activePreset: Preset;
  select(id: string): void;
  addCustom(name: string, config: TimerConfig): void;
  deleteCustom(id: string): void;
}

export function usePresets(): PresetsApi {
  const [, force] = useState(0);
  useEffect(() => {
    const l = () => force((n) => n + 1);
    listeners.add(l);
    return () => {
      listeners.delete(l);
    };
  }, []);
  return {
    all: getAll(),
    activeId,
    activePreset: getActivePreset(),
    select(id: string) {
      activeId = id;
      writeActiveId(id);
      notify();
    },
    addCustom(name: string, config: TimerConfig) {
      const id = newCustomId();
      customs = [...customs, { id, name: name.trim() || "Custom", config, isBuiltIn: false }];
      writeCustoms(customs);
      notify();
    },
    deleteCustom(id: string) {
      customs = customs.filter((p) => p.id !== id);
      writeCustoms(customs);
      if (activeId === id) {
        activeId = DEFAULT_PRESET.id;
        writeActiveId(activeId);
      }
      notify();
    },
  };
}
