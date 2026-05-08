import { Outlet } from "react-router-dom";
import { BottomNav } from "./BottomNav";

export function Shell() {
  return (
    <div
      style={{
        minHeight: "100dvh",
        display: "flex",
        flexDirection: "column",
        background: "var(--paper)",
      }}
    >
      <main
        style={{
          flex: 1,
          maxWidth: "var(--content-max)",
          width: "100%",
          margin: "0 auto",
          padding: "16px 16px calc(var(--bottomnav-h) + 24px)",
        }}
      >
        <Outlet />
      </main>
      <BottomNav />
    </div>
  );
}
