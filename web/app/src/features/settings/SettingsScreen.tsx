import { useSettings } from "@/data/settings";
import { Locale, useTranslation } from "@/i18n";
import { useTheme, ThemeMode } from "@/theme/ThemeProvider";

export function SettingsScreen() {
  const { settings, setSound, setHaptics, setKeepScreenOn } = useSettings();
  const { t, locale, setLocale } = useTranslation();
  const { mode, setMode } = useTheme();

  return (
    <div style={{ paddingTop: 16, display: "flex", flexDirection: "column", gap: 8 }}>
      <h1 style={{ margin: 0, fontSize: 24, fontWeight: 800, marginBottom: 8 }}>{t("settings_title")}</h1>

      <Toggle
        label={t("settings_sound")}
        description={t("settings_sound_desc")}
        checked={settings.soundEnabled}
        onChange={setSound}
      />
      <Toggle
        label={t("settings_vibration")}
        description={t("settings_vibration_desc")}
        checked={settings.hapticsEnabled}
        onChange={setHaptics}
      />
      <Toggle
        label={t("settings_keep_screen_on")}
        description={t("settings_keep_screen_on_desc")}
        checked={settings.keepScreenOn}
        onChange={setKeepScreenOn}
      />

      <SegmentedSetting
        label={t("settings_theme")}
        description={t("settings_theme_desc")}
        value={mode}
        options={[
          { value: "system", label: t("settings_theme_system") },
          { value: "light", label: t("settings_theme_light") },
          { value: "dark", label: t("settings_theme_dark") },
        ]}
        onChange={(v) => setMode(v as ThemeMode)}
      />

      <SegmentedSetting
        label={t("settings_language")}
        description={t("settings_language_desc")}
        value={locale}
        options={[
          { value: "en", label: "English" },
          { value: "uk", label: "Українська" },
          { value: "ja", label: "日本語" },
        ]}
        onChange={(v) => setLocale(v as Locale)}
      />
    </div>
  );
}

function Toggle({
  label,
  description,
  checked,
  onChange,
}: {
  label: string;
  description: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <label
      style={{
        display: "flex",
        alignItems: "center",
        gap: 12,
        background: "var(--paper-2)",
        borderRadius: "var(--r-md)",
        padding: 16,
        cursor: "pointer",
      }}
    >
      <span style={{ flex: 1, display: "flex", flexDirection: "column", gap: 2 }}>
        <span style={{ fontSize: 15, fontWeight: 700, color: "var(--ink)" }}>{label}</span>
        <span style={{ fontSize: 12, color: "var(--ink-3)" }}>{description}</span>
      </span>
      <span
        role="switch"
        aria-checked={checked}
        onClick={(e) => {
          e.preventDefault();
          onChange(!checked);
        }}
        style={{
          position: "relative",
          width: 44,
          height: 26,
          borderRadius: 999,
          background: checked ? "var(--accent)" : "var(--line-2)",
          transition: "background 150ms ease",
          flexShrink: 0,
        }}
      >
        <span
          style={{
            position: "absolute",
            top: 3,
            left: checked ? 21 : 3,
            width: 20,
            height: 20,
            borderRadius: "50%",
            background: "#fff",
            boxShadow: "0 1px 2px rgba(0,0,0,0.2)",
            transition: "left 150ms ease",
          }}
        />
      </span>
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        style={{ position: "absolute", opacity: 0, pointerEvents: "none" }}
      />
    </label>
  );
}

function SegmentedSetting({
  label,
  description,
  value,
  options,
  onChange,
}: {
  label: string;
  description: string;
  value: string;
  options: { value: string; label: string }[];
  onChange: (v: string) => void;
}) {
  return (
    <div
      style={{
        background: "var(--paper-2)",
        borderRadius: "var(--r-md)",
        padding: 16,
        display: "flex",
        flexDirection: "column",
        gap: 10,
      }}
    >
      <div>
        <div style={{ fontSize: 15, fontWeight: 700, color: "var(--ink)" }}>{label}</div>
        <div style={{ fontSize: 12, color: "var(--ink-3)" }}>{description}</div>
      </div>
      <div
        role="radiogroup"
        aria-label={label}
        style={{
          display: "flex",
          background: "var(--paper)",
          borderRadius: "var(--r-sm)",
          padding: 3,
          gap: 2,
          border: "1px solid var(--line)",
        }}
      >
        {options.map((opt) => {
          const active = opt.value === value;
          return (
            <button
              key={opt.value}
              role="radio"
              aria-checked={active}
              onClick={() => onChange(opt.value)}
              style={{
                flex: 1,
                padding: "8px 10px",
                fontSize: 13,
                fontWeight: 600,
                border: "none",
                borderRadius: "calc(var(--r-sm) - 2px)",
                background: active ? "var(--accent)" : "transparent",
                color: active ? "#fff" : "var(--ink-2)",
                cursor: "pointer",
                transition: "background 150ms ease, color 150ms ease",
              }}
            >
              {opt.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}
