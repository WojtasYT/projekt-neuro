import { useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { db, ensureSeeded } from './db/database';
import { applyTheme } from './services/profile';
import { AppShell } from './components/AppShell';
import { Onboarding } from './components/Onboarding';
import { CaptureProvider } from './components/CaptureSheet';
import { LabScreen } from './screens/LabScreen';
import { ThoughtsScreen } from './screens/ThoughtsScreen';
import { StudioScreen } from './screens/StudioScreen';
import { ProjectsScreen } from './screens/ProjectsScreen';
import { ProjectDetailScreen } from './screens/ProjectDetailScreen';
import { GraphScreen } from './screens/GraphScreen';
import { ExploreScreen } from './screens/ExploreScreen';
import { LibraryScreen } from './screens/LibraryScreen';
import { BookScreen } from './screens/BookScreen';
import { SettingsScreen } from './screens/SettingsScreen';

export default function App() {
  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string>();
  const profile = useLiveQuery(() => db.profile.get('local'), []);

  useEffect(() => {
    ensureSeeded()
      .then(() => setReady(true))
      .catch(() => setError('Nie udało się otworzyć lokalnej bazy.'));
  }, []);

  useEffect(() => {
    if (profile?.theme) applyTheme(profile.theme);
  }, [profile?.theme]);

  if (error) {
    return (
      <div className="page">
        <div className="notice">{error}</div>
      </div>
    );
  }

  if (!ready) {
    return (
      <div className="page">
        <p className="muted">Otwieram laboratorium…</p>
      </div>
    );
  }

  return (
    <CaptureProvider>
      {!profile?.onboarded ? <Onboarding /> : null}
      <AppShell>
        <Routes>
          <Route path="/" element={<LabScreen />} />
          <Route path="/mysli" element={<ThoughtsScreen />} />
          <Route path="/mysli/:id" element={<StudioScreen />} />
          <Route path="/projekty" element={<ProjectsScreen />} />
          <Route path="/projekty/:id" element={<ProjectDetailScreen />} />
          <Route path="/polaczenia" element={<GraphScreen />} />
          <Route path="/eksploracja" element={<ExploreScreen />} />
          <Route path="/biblioteka" element={<LibraryScreen />} />
          <Route path="/biblioteka/:id" element={<BookScreen />} />
          <Route path="/ustawienia" element={<SettingsScreen />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AppShell>
    </CaptureProvider>
  );
}
