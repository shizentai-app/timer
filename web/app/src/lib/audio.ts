import { AudioCue } from "./timer";

const SOURCES: Record<AudioCue, string> = {
  work_start: "/sounds/gong_twice.wav",
  work_warning: "/sounds/alert.wav",
  rest_start: "/sounds/gong.wav",
  rest_ending: "/sounds/rest_end.wav",
  finished: "/sounds/gong.wav",
};

const cache = new Map<AudioCue, HTMLAudioElement>();

function load(cue: AudioCue): HTMLAudioElement {
  let a = cache.get(cue);
  if (!a) {
    a = new Audio(SOURCES[cue]);
    a.preload = "auto";
    cache.set(cue, a);
  }
  return a;
}

/**
 * Browsers refuse to play audio until the user interacts with the page. Call
 * this from a click handler on the timer's primary action so the cue audio is
 * unlocked once and stays unlocked for the session.
 */
export function unlockAudio(): void {
  for (const cue of Object.keys(SOURCES) as AudioCue[]) {
    const a = load(cue);
    // `play().catch()` triggers the autoplay-policy unlock without actually
    // emitting sound: we pause and rewind immediately.
    a.muted = true;
    a.play()
      .then(() => {
        a.pause();
        a.currentTime = 0;
        a.muted = false;
      })
      .catch(() => {
        a.muted = false;
      });
  }
}

export function playCue(cue: AudioCue): void {
  const a = load(cue);
  // Allow overlapping cues by cloning when the cached node is busy.
  if (!a.paused) {
    const clone = a.cloneNode(true) as HTMLAudioElement;
    clone.play().catch(() => {});
    return;
  }
  a.currentTime = 0;
  a.play().catch(() => {});
}
