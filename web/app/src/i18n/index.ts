import { useEffect, useState } from "react";

export type Locale = "en" | "uk" | "ja";

export interface Strings {
  nav_timer: string;
  nav_presets: string;
  nav_settings: string;

  phase_idle: string;
  phase_prepare: string;
  phase_work: string;
  phase_warning: string;
  phase_rest: string;
  phase_finished: string;

  action_start: string;
  action_pause: string;
  action_resume: string;
  action_reset: string;
  action_skip: string;
  action_restart: string;
  action_create: string;
  action_cancel: string;
  action_delete: string;

  round_progress: string; // "Round {round}/{total}"
  rounds_summary: string; // "{rounds} rounds · {work}s work / {rest}s rest"
  finished_summary: string; // "Done · {round}/{total}"

  settings_title: string;
  settings_sound: string;
  settings_sound_desc: string;
  settings_vibration: string;
  settings_vibration_desc: string;
  settings_keep_screen_on: string;
  settings_keep_screen_on_desc: string;
  settings_language: string;
  settings_language_desc: string;
  settings_theme: string;
  settings_theme_desc: string;
  settings_theme_system: string;
  settings_theme_light: string;
  settings_theme_dark: string;

  presets_title: string;
  presets_new_title: string;
  field_name: string;
  field_work: string;
  field_rest: string;
  field_rounds: string;
  field_prep: string;
  field_warning: string;
}

const en: Strings = {
  nav_timer: "Timer",
  nav_presets: "Presets",
  nav_settings: "Settings",
  phase_idle: "Ready",
  phase_prepare: "Yoi!",
  phase_work: "Hajime!",
  phase_warning: "Ato sukoshi!",
  phase_rest: "Yame!",
  phase_finished: "Owari",
  action_start: "Start",
  action_pause: "Pause",
  action_resume: "Resume",
  action_reset: "Reset",
  action_skip: "Skip",
  action_restart: "Restart",
  action_create: "Create",
  action_cancel: "Cancel",
  action_delete: "Delete",
  round_progress: "Round {round}/{total}",
  rounds_summary: "{rounds} rounds · {work}s work / {rest}s rest",
  finished_summary: "Done · {round}/{total}",
  settings_title: "Settings",
  settings_sound: "Sound",
  settings_sound_desc: "Play cue sounds on phase changes",
  settings_vibration: "Vibration",
  settings_vibration_desc: "Buzz on phase changes (mobile only)",
  settings_keep_screen_on: "Keep screen on",
  settings_keep_screen_on_desc: "Prevent the screen from sleeping during a workout",
  settings_language: "Language",
  settings_language_desc: "Interface language",
  settings_theme: "Theme",
  settings_theme_desc: "Light, dark, or follow system",
  settings_theme_system: "System",
  settings_theme_light: "Light",
  settings_theme_dark: "Dark",
  presets_title: "Presets",
  presets_new_title: "New preset",
  field_name: "Name",
  field_work: "Work (s)",
  field_rest: "Rest (s)",
  field_rounds: "Rounds",
  field_prep: "Prep (s)",
  field_warning: "Warning (s)",
};

const uk: Strings = {
  nav_timer: "Таймер",
  nav_presets: "Пресети",
  nav_settings: "Налаштування",
  phase_idle: "Готовий",
  phase_prepare: "Йоі!",
  phase_work: "Хаджіме!",
  phase_warning: "Ато сукоші!",
  phase_rest: "Ямé!",
  phase_finished: "Завершено",
  action_start: "Старт",
  action_pause: "Пауза",
  action_resume: "Продовжити",
  action_reset: "Скинути",
  action_skip: "Пропустити",
  action_restart: "Спочатку",
  action_create: "Створити",
  action_cancel: "Скасувати",
  action_delete: "Видалити",
  round_progress: "Раунд {round}/{total}",
  rounds_summary: "{rounds} раунди · {work}с робота / {rest}с відпочинок",
  finished_summary: "Завершено · {round}/{total}",
  settings_title: "Налаштування",
  settings_sound: "Звук",
  settings_sound_desc: "Звукові сигнали зміни фаз",
  settings_vibration: "Вібрація",
  settings_vibration_desc: "Вібрація на зміні фаз (тільки мобільний)",
  settings_keep_screen_on: "Тримати екран увімкненим",
  settings_keep_screen_on_desc: "Не давати екрану засинати під час тренування",
  settings_language: "Мова",
  settings_language_desc: "Мова інтерфейсу",
  settings_theme: "Тема",
  settings_theme_desc: "Світла, темна або як у системі",
  settings_theme_system: "Системна",
  settings_theme_light: "Світла",
  settings_theme_dark: "Темна",
  presets_title: "Пресети",
  presets_new_title: "Новий пресет",
  field_name: "Назва",
  field_work: "Робота (с)",
  field_rest: "Відпочинок (с)",
  field_rounds: "Раунди",
  field_prep: "Підготовка (с)",
  field_warning: "Попередження (с)",
};

const ja: Strings = {
  nav_timer: "タイマー",
  nav_presets: "プリセット",
  nav_settings: "設定",
  phase_idle: "準備中",
  phase_prepare: "用意!",
  phase_work: "始め!",
  phase_warning: "あと少し!",
  phase_rest: "止め!",
  phase_finished: "終わり",
  action_start: "スタート",
  action_pause: "一時停止",
  action_resume: "再開",
  action_reset: "リセット",
  action_skip: "スキップ",
  action_restart: "再スタート",
  action_create: "作成",
  action_cancel: "キャンセル",
  action_delete: "削除",
  round_progress: "ラウンド {round}/{total}",
  rounds_summary: "{rounds}ラウンド · {work}秒運動 / {rest}秒休憩",
  finished_summary: "完了 · {round}/{total}",
  settings_title: "設定",
  settings_sound: "音",
  settings_sound_desc: "フェーズ切り替え時に音を鳴らす",
  settings_vibration: "バイブレーション",
  settings_vibration_desc: "フェーズ切り替え時に振動 (モバイルのみ)",
  settings_keep_screen_on: "画面をオンに保つ",
  settings_keep_screen_on_desc: "トレーニング中、画面がスリープしないようにする",
  settings_language: "言語",
  settings_language_desc: "インターフェースの言語",
  settings_theme: "テーマ",
  settings_theme_desc: "ライト、ダーク、またはシステムに従う",
  settings_theme_system: "システム",
  settings_theme_light: "ライト",
  settings_theme_dark: "ダーク",
  presets_title: "プリセット",
  presets_new_title: "新しいプリセット",
  field_name: "名前",
  field_work: "運動 (秒)",
  field_rest: "休憩 (秒)",
  field_rounds: "ラウンド",
  field_prep: "準備 (秒)",
  field_warning: "警告 (秒)",
};

const DICTS: Record<Locale, Strings> = { en, uk, ja };
const STORAGE_KEY = "shizentai-timer.locale";

function detectLocale(): Locale {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === "en" || stored === "uk" || stored === "ja") return stored;
  } catch {
    // ignore
  }
  if (typeof navigator !== "undefined") {
    const lang = navigator.language.toLowerCase();
    if (lang.startsWith("uk")) return "uk";
    if (lang.startsWith("ja")) return "ja";
  }
  return "en";
}

let current: Locale = detectLocale();
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((l) => l());
}

export function getLocale(): Locale {
  return current;
}

function persistLocale(locale: Locale) {
  try {
    localStorage.setItem(STORAGE_KEY, locale);
    document.documentElement.setAttribute("lang", locale);
  } catch {
    // ignore
  }
}

persistLocale(current); // ensure <html lang> matches initial detection

export function setLocale(locale: Locale): void {
  if (current === locale) return;
  current = locale;
  persistLocale(locale);
  notify();
}

type TArgs = Record<string, string | number>;

function interpolate(template: string, args?: TArgs): string {
  if (!args) return template;
  return template.replace(/\{(\w+)\}/g, (_, key) => String(args[key] ?? `{${key}}`));
}

export interface I18n {
  locale: Locale;
  setLocale(locale: Locale): void;
  t(key: keyof Strings, args?: TArgs): string;
}

export function useTranslation(): I18n {
  const [, force] = useState(0);
  useEffect(() => {
    const l = () => force((n) => n + 1);
    listeners.add(l);
    return () => {
      listeners.delete(l);
    };
  }, []);
  const dict = DICTS[current];
  return {
    locale: current,
    setLocale,
    t(key, args) {
      return interpolate(dict[key], args);
    },
  };
}
