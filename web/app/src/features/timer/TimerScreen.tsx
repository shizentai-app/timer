import { useEffect, useMemo } from "react";
import { usePresets } from "@/data/presets";
import { useSettings } from "@/data/settings";
import { Strings, useTranslation } from "@/i18n";
import { unlockAudio } from "@/lib/audio";
import { Phase, formatMmSs } from "@/lib/timer";
import { useWakeLock } from "@/lib/wakeLock";
import { useTimerEngine } from "./useTimerEngine";

function phaseBackground(phase: Phase): string {
  switch (phase) {
    case "WORK":
      return "var(--phase-work)";
    case "WARNING":
      return "var(--phase-warning)";
    case "REST":
      return "var(--phase-rest)";
    case "PREPARE":
      return "var(--phase-prepare)";
    case "FINISHED":
      return "var(--phase-finished)";
    default:
      return "var(--paper)";
  }
}

function phaseInk(phase: Phase): string {
  switch (phase) {
    case "WORK":
    case "WARNING":
    case "REST":
    case "PREPARE":
    case "FINISHED":
      return "#FFFFFF";
    default:
      return "var(--ink)";
  }
}

function phaseKey(phase: Phase): keyof Strings {
  switch (phase) {
    case "PREPARE":
      return "phase_prepare";
    case "WORK":
      return "phase_work";
    case "WARNING":
      return "phase_warning";
    case "REST":
      return "phase_rest";
    case "FINISHED":
      return "phase_finished";
    default:
      return "phase_idle";
  }
}

// Resolves CSS var to a literal hex so it can be written to the
// <meta name="theme-color"> tag (mobile browser chrome can't use vars).
function resolveCssColor(value: string): string {
  if (!value.startsWith("var(")) return value;
  const m = /^var\((--[^)]+)\)$/.exec(value);
  if (!m) return value;
  return getComputedStyle(document.documentElement).getPropertyValue(m[1]).trim();
}

function useThemeColorMeta(color: string): void {
  useEffect(() => {
    const resolved = resolveCssColor(color);
    if (!resolved) return;
    let meta = document.querySelector<HTMLMetaElement>('meta[name="theme-color"]');
    if (!meta) {
      meta = document.createElement("meta");
      meta.name = "theme-color";
      document.head.appendChild(meta);
    }
    const previous = meta.content;
    meta.content = resolved;
    return () => {
      meta!.content = previous;
    };
  }, [color]);
}

export function TimerScreen() {
  const { state, start, pause, resume, reset, skip } = useTimerEngine();
  const { activePreset } = usePresets();
  const { settings } = useSettings();
  const { t } = useTranslation();
  useWakeLock(state.isRunning && settings.keepScreenOn);

  const isIdle = state.phase === "IDLE";
  const isFinished = state.phase === "FINISHED";
  const isRunning = state.isRunning;
  const isPaused = !isRunning && !isIdle && !isFinished;

  const bg = useMemo(() => phaseBackground(state.phase), [state.phase]);
  const ink = useMemo(() => phaseInk(state.phase), [state.phase]);
  useThemeColorMeta(bg);

  const display = isIdle
    ? formatMmSs(activePreset.config.workSeconds * 1000)
    : formatMmSs(state.remainingMs);

  const handleStart = () => {
    unlockAudio();
    start(activePreset.config);
  };

  return (
    <div
      style={{
        minHeight: "100dvh",
        // Bleed beyond the <main> padding so the phase color reaches every edge.
        margin: "0 calc(50% - 50vw)",
        marginTop: "-16px",
        width: "100vw",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "32px 24px calc(env(safe-area-inset-bottom) + 32px)",
        background: bg,
        color: ink,
        transition: "background 220ms ease, color 220ms ease",
      }}
    >
      {/* Top zone: phase command (during a phase) or preset name (idle). */}
      <div
        style={{
          minHeight: 72,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        {isIdle ? (
          <div
            style={{
              fontSize: 16,
              fontWeight: 700,
              letterSpacing: "0.12em",
              textTransform: "uppercase",
              opacity: 0.85,
            }}
          >
            {activePreset.name}
          </div>
        ) : (
          <div
            style={{
              fontSize: "clamp(28px, 7vw, 40px)",
              fontWeight: 800,
              letterSpacing: "0.06em",
              lineHeight: 1.1,
              textAlign: "center",
            }}
          >
            {t(phaseKey(state.phase))}
          </div>
        )}
      </div>

      {/* Middle zone: large digit + round counter. */}
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: 8,
          paddingBottom: 24,
        }}
      >
        <div
          style={{
            fontSize: "clamp(120px, 32vw, 220px)",
            fontWeight: 800,
            letterSpacing: "-0.03em",
            lineHeight: 1,
            fontVariantNumeric: "tabular-nums",
            textAlign: "center",
          }}
        >
          {display}
        </div>
        <div style={{ fontSize: 15, fontWeight: 600, opacity: 0.85, textAlign: "center" }}>
          {isIdle
            ? t("rounds_summary", {
                rounds: activePreset.config.rounds,
                work: activePreset.config.workSeconds,
                rest: activePreset.config.restSeconds,
              })
            : isFinished
              ? t("finished_summary", { round: state.totalRounds, total: state.totalRounds })
              : t("round_progress", { round: Math.max(state.round, 1), total: state.totalRounds })}
        </div>
      </div>

      {/* Bottom zone: actions. Skip + Reset only show when paused. */}
      <div style={{ display: "flex", gap: 12, flexWrap: "wrap", justifyContent: "center" }}>
        {isIdle || isFinished ? (
          <PrimaryButton onClick={handleStart} ink={ink}>
            {isFinished ? t("action_restart") : t("action_start")}
          </PrimaryButton>
        ) : isRunning ? (
          <PrimaryButton onClick={pause} ink={ink}>
            {t("action_pause")}
          </PrimaryButton>
        ) : (
          <PrimaryButton onClick={resume} ink={ink}>
            {t("action_resume")}
          </PrimaryButton>
        )}

        {isPaused && (
          <>
            <SecondaryButton onClick={skip} ink={ink}>
              {t("action_skip")}
            </SecondaryButton>
            <SecondaryButton onClick={reset} ink={ink}>
              {t("action_reset")}
            </SecondaryButton>
          </>
        )}
      </div>
    </div>
  );
}

function PrimaryButton({
  children,
  onClick,
  ink,
}: {
  children: React.ReactNode;
  onClick: () => void;
  ink: string;
}) {
  const isWhiteInk = ink === "#FFFFFF";
  return (
    <button
      onClick={onClick}
      style={{
        background: isWhiteInk ? "#FFFFFF" : "var(--ink)",
        // White pill always carries dark text; var(--ink) flips to cream in
        // dark mode and would be invisible on white. Tone-locked.
        color: isWhiteInk ? "#1A1A1A" : "var(--paper)",
        border: "none",
        borderRadius: "var(--r-md)",
        padding: "16px 32px",
        fontSize: 17,
        fontWeight: 700,
        letterSpacing: "0.01em",
        cursor: "pointer",
        minWidth: 160,
      }}
    >
      {children}
    </button>
  );
}

function SecondaryButton({
  children,
  onClick,
  ink,
}: {
  children: React.ReactNode;
  onClick: () => void;
  ink: string;
}) {
  const isWhiteInk = ink === "#FFFFFF";
  return (
    <button
      onClick={onClick}
      style={{
        background: "transparent",
        color: ink,
        border: `1.5px solid ${isWhiteInk ? "rgba(255,255,255,0.6)" : "var(--line)"}`,
        borderRadius: "var(--r-md)",
        padding: "12px 18px",
        fontSize: 14,
        fontWeight: 600,
        cursor: "pointer",
      }}
    >
      {children}
    </button>
  );
}
