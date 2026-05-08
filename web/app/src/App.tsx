import { Navigate, Route, Routes } from "react-router-dom";
import { Shell } from "./components/Shell";
import { TimerScreen } from "./features/timer/TimerScreen";
import { PresetsScreen } from "./features/presets/PresetsScreen";
import { SettingsScreen } from "./features/settings/SettingsScreen";

export function App() {
  return (
    <Routes>
      <Route element={<Shell />}>
        <Route path="/" element={<TimerScreen />} />
        <Route path="/presets" element={<PresetsScreen />} />
        <Route path="/settings" element={<SettingsScreen />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
