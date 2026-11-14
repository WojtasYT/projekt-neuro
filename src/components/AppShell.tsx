import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useOnline } from '../hooks/useOnline';
import { IconGraph, IconLab, IconMark, IconMore, IconProjects, IconThoughts } from './Icons';
import { useCapture } from './CaptureSheet';

const nav = [
  { to: '/', label: 'Laboratorium', icon: <IconLab />, end: true },
  { to: '/mysli', label: 'Myśli', icon: <IconThoughts /> },
  { to: '/polaczenia', label: 'Połączenia', icon: <IconGraph /> },
  { to: '/projekty', label: 'Projekty', icon: <IconProjects /> },
  { to: '/ustawienia', label: 'Więcej', icon: <IconMore /> },
];

export function AppShell({ children }: { children: ReactNode }) {
  const online = useOnline();
  const capture = useCapture();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink to="/" className="brand" end>
          <IconMark />
          <span>
            <span className="brand-name">Neurotrino Lab</span>
            <span className="brand-sub">laboratorium myśli</span>
          </span>
        </NavLink>
        {nav.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
            {item.label}
          </NavLink>
        ))}
        <NavLink to="/eksploracja">Eksploracja</NavLink>
        <NavLink to="/biblioteka">Biblioteka</NavLink>
        <div style={{ marginTop: 'auto' }}>
          <button className="btn primary" type="button" onClick={() => capture.open()}>
            Zapisz myśl
          </button>
        </div>
      </aside>
      <div>
        <header className="topbar">
          <NavLink to="/" className="brand" end>
            <IconMark />
            <span>
              <span className="brand-name">Neurotrino Lab</span>
              <span className="brand-sub">prywatne · lokalne</span>
            </span>
          </NavLink>
          <button className="btn primary" type="button" onClick={() => capture.open()}>
            Zapisz myśl
          </button>
        </header>
        {!online ? (
          <div className="offline-banner">
            Jesteś offline. Zapis, wyszukiwanie i mapa działają lokalnie. AI i sieć — nie.
          </div>
        ) : null}
        {children}
      </div>
      <nav className="bottom-nav" aria-label="Główne">
        {nav.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
            {item.icon}
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
