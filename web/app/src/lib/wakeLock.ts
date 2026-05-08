import { useEffect } from "react";

/**
 * Holds a screen wake lock while [active] is true. No-op on browsers that
 * don't support the Wake Lock API. Re-acquires on visibility change so the
 * lock survives tab-switching back to the page.
 */
export function useWakeLock(active: boolean): void {
  useEffect(() => {
    if (!active) return;
    type WakeLockSentinel = { release: () => Promise<void> };
    const wakeLockApi = (
      navigator as Navigator & {
        wakeLock?: { request: (kind: "screen") => Promise<WakeLockSentinel> };
      }
    ).wakeLock;
    if (!wakeLockApi) return;

    let sentinel: WakeLockSentinel | null = null;
    let cancelled = false;

    const acquire = async () => {
      try {
        const s = await wakeLockApi.request("screen");
        if (cancelled) {
          await s.release();
        } else {
          sentinel = s;
        }
      } catch {
        // user gesture missing, permission denied, etc.
      }
    };

    const onVisibility = () => {
      if (document.visibilityState === "visible") void acquire();
    };

    void acquire();
    document.addEventListener("visibilitychange", onVisibility);

    return () => {
      cancelled = true;
      document.removeEventListener("visibilitychange", onVisibility);
      sentinel?.release().catch(() => {});
      sentinel = null;
    };
  }, [active]);
}
