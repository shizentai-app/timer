import { AudioCue } from "./timer";

function safeVibrate(pattern: number | number[]): void {
  if (typeof navigator === "undefined" || typeof navigator.vibrate !== "function") return;
  try {
    navigator.vibrate(pattern);
  } catch {
    // some browsers reject under certain policies; ignore silently
  }
}

export function buzzForCue(cue: AudioCue): void {
  switch (cue) {
    case "work_start":
    case "rest_start":
    case "finished":
      safeVibrate(60);
      break;
    case "work_warning":
    case "rest_ending":
      safeVibrate(35);
      break;
  }
}
