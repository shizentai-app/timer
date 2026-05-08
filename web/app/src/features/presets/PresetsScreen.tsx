import { useState } from "react";
import { Preset, usePresets } from "@/data/presets";
import { useTranslation } from "@/i18n";
import { TimerConfig } from "@/lib/timer";

export function PresetsScreen() {
  const { all, activeId, select, deleteCustom, addCustom } = usePresets();
  const { t } = useTranslation();
  const [editorOpen, setEditorOpen] = useState(false);

  return (
    <div style={{ paddingTop: 16 }}>
      <header
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: 16,
        }}
      >
        <h1 style={{ margin: 0, fontSize: 24, fontWeight: 800 }}>{t("presets_title")}</h1>
        <button
          onClick={() => setEditorOpen(true)}
          style={{
            background: "var(--accent)",
            color: "#fff",
            border: "none",
            borderRadius: 999,
            width: 44,
            height: 44,
            fontSize: 24,
            fontWeight: 700,
            cursor: "pointer",
          }}
          aria-label="Add preset"
        >
          +
        </button>
      </header>

      <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "flex", flexDirection: "column", gap: 8 }}>
        {all.map((preset) => (
          <PresetRow
            key={preset.id}
            preset={preset}
            isActive={preset.id === activeId}
            onSelect={() => select(preset.id)}
            onDelete={preset.isBuiltIn ? undefined : () => deleteCustom(preset.id)}
          />
        ))}
      </ul>

      {editorOpen && (
        <AddPresetDialog
          onCancel={() => setEditorOpen(false)}
          onCreate={(name, cfg) => {
            addCustom(name, cfg);
            setEditorOpen(false);
          }}
        />
      )}
    </div>
  );
}

function PresetRowDelete({ onDelete, label }: { onDelete: () => void; label: string }) {
  return (
    <span
      role="button"
      tabIndex={0}
      onClick={(e) => {
        e.stopPropagation();
        onDelete();
      }}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          e.stopPropagation();
          onDelete();
        }
      }}
      style={{ color: "var(--accent)", fontSize: 13, fontWeight: 600, cursor: "pointer", padding: "4px 8px" }}
    >
      {label}
    </span>
  );
}

function PresetRow({
  preset,
  isActive,
  onSelect,
  onDelete,
}: {
  preset: Preset;
  isActive: boolean;
  onSelect: () => void;
  onDelete?: () => void;
}) {
  const { t } = useTranslation();
  return (
    <li>
      <button
        onClick={onSelect}
        style={{
          width: "100%",
          display: "flex",
          alignItems: "center",
          gap: 12,
          background: "var(--paper-2)",
          border: `1.5px solid ${isActive ? "var(--accent)" : "transparent"}`,
          borderRadius: "var(--r-md)",
          padding: 14,
          cursor: "pointer",
          textAlign: "left",
          color: "var(--ink)",
        }}
      >
        <span
          aria-hidden
          style={{
            width: 14,
            height: 14,
            borderRadius: "50%",
            background: isActive ? "var(--accent)" : "transparent",
            border: isActive ? "none" : "1.5px solid var(--line-2)",
          }}
        />
        <span style={{ flex: 1, display: "flex", flexDirection: "column", gap: 2 }}>
          <span style={{ fontSize: 15, fontWeight: 700 }}>{preset.name}</span>
          <span style={{ fontSize: 13, color: "var(--ink-3)" }}>
            {preset.config.rounds} rounds · {preset.config.workSeconds}s / {preset.config.restSeconds}s
          </span>
        </span>
        {onDelete && <PresetRowDelete onDelete={onDelete} label={t("action_delete")} />}
      </button>
    </li>
  );
}

function AddPresetDialog({
  onCancel,
  onCreate,
}: {
  onCancel: () => void;
  onCreate: (name: string, config: TimerConfig) => void;
}) {
  const { t } = useTranslation();
  const [name, setName] = useState("");
  const [work, setWork] = useState(60);
  const [rest, setRest] = useState(30);
  const [rounds, setRounds] = useState(3);
  const [prep, setPrep] = useState(10);
  const [warning, setWarning] = useState(10);

  return (
    <div
      role="dialog"
      aria-modal="true"
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.5)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
        zIndex: 50,
      }}
      onClick={onCancel}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: "var(--paper)",
          color: "var(--ink)",
          borderRadius: "var(--r-lg)",
          padding: 20,
          maxWidth: 360,
          width: "100%",
          display: "flex",
          flexDirection: "column",
          gap: 12,
        }}
      >
        <h2 style={{ margin: 0, fontSize: 17, fontWeight: 800 }}>{t("presets_new_title")}</h2>
        <NumberOrTextField label={t("field_name")} value={name} onChange={setName} />
        <NumberField label={t("field_work")} value={work} onChange={setWork} min={1} />
        <NumberField label={t("field_rest")} value={rest} onChange={setRest} min={0} />
        <NumberField label={t("field_rounds")} value={rounds} onChange={setRounds} min={1} />
        <NumberField label={t("field_prep")} value={prep} onChange={setPrep} min={0} />
        <NumberField label={t("field_warning")} value={warning} onChange={setWarning} min={0} />
        <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", marginTop: 8 }}>
          <button onClick={onCancel} style={ghostBtn}>
            {t("action_cancel")}
          </button>
          <button
            onClick={() =>
              onCreate(name, {
                workSeconds: work,
                restSeconds: rest,
                rounds,
                prepSeconds: prep,
                warningSeconds: warning,
                signalEndOfRest: true,
              })
            }
            style={primaryBtn}
          >
            {t("action_create")}
          </button>
        </div>
      </div>
    </div>
  );
}

const ghostBtn: React.CSSProperties = {
  background: "transparent",
  color: "var(--ink)",
  border: "1.5px solid var(--line)",
  borderRadius: "var(--r-md)",
  padding: "10px 14px",
  fontSize: 14,
  fontWeight: 600,
  cursor: "pointer",
};

const primaryBtn: React.CSSProperties = {
  background: "var(--accent)",
  color: "#fff",
  border: "none",
  borderRadius: "var(--r-md)",
  padding: "10px 18px",
  fontSize: 14,
  fontWeight: 700,
  cursor: "pointer",
};

function NumberOrTextField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <label style={fieldWrap}>
      <span style={fieldLabel}>{label}</span>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={fieldInput}
      />
    </label>
  );
}

function NumberField({
  label,
  value,
  onChange,
  min,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
  min?: number;
}) {
  return (
    <label style={fieldWrap}>
      <span style={fieldLabel}>{label}</span>
      <input
        type="number"
        inputMode="numeric"
        value={value}
        min={min}
        onChange={(e) => {
          const n = Number(e.target.value);
          if (Number.isFinite(n)) onChange(min !== undefined ? Math.max(min, n) : n);
        }}
        style={fieldInput}
      />
    </label>
  );
}

const fieldWrap: React.CSSProperties = { display: "flex", flexDirection: "column", gap: 4 };
const fieldLabel: React.CSSProperties = {
  fontSize: 11,
  fontWeight: 700,
  letterSpacing: "0.06em",
  textTransform: "uppercase",
  color: "var(--ink-3)",
};
const fieldInput: React.CSSProperties = {
  background: "var(--paper-2)",
  color: "var(--ink)",
  border: "1px solid var(--line)",
  borderRadius: "var(--r-sm)",
  padding: "10px 12px",
  fontSize: 14,
  fontWeight: 600,
  fontFamily: "inherit",
};
