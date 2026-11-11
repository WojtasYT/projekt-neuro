import { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { db, removeSampleContent, resetLocalData } from '../db/database';
import { aiModeLabels, defaultInterests } from '../domain/labels';
import { AI_MODES, type AiMode } from '../domain/types';
import { applyTheme, updateProfile } from '../services/profile';
import { buildExport, exportToJson, importLabData } from '../services/exportImport';

export function SettingsScreen() {
  const profile = useLiveQuery(() => db.profile.get('local'), []);
  const fileRef = useRef<HTMLInputElement>(null);
  const [message, setMessage] = useState<string>();
  const [gateway, setGateway] = useState<string>();
  const [model, setModel] = useState<string>();

  if (!profile) {
    return (
      <main className="page">
        <p className="muted">Wczytuję profil…</p>
      </main>
    );
  }

  const gatewayValue = gateway ?? profile.aiConfig.gatewayUrl;
  const modelValue = model ?? profile.aiConfig.model ?? '';

  return (
    <main className="page stack">
      <section className="hero">
        <p className="kicker">Profil i prywatność</p>
        <h1>Więcej</h1>
        <p className="lede">Pamięć jest lokalna i edytowalna. Nic nie wysyła się samo.</p>
      </section>

      <nav className="chip-row">
        <Link className="chip" to="/eksploracja">
          Eksploracja
        </Link>
        <Link className="chip" to="/biblioteka">
          Biblioteka
        </Link>
      </nav>

      <section className="card stack">
        <h2>O Tobie — tylko to, co chcesz tu mieć</h2>
        <label>
          Jak się do Ciebie zwracać (opcjonalnie)
          <input
            className="field"
            defaultValue={profile.displayName}
            onBlur={(e) => updateProfile({ displayName: e.target.value })}
          />
        </label>
        <label>
          Aktualne ognisko uwagi
          <input
            className="field"
            defaultValue={profile.currentFocus}
            onBlur={(e) => updateProfile({ currentFocus: e.target.value })}
          />
        </label>
        <label>
          Notatka do siebie
          <textarea
            className="field"
            rows={3}
            defaultValue={profile.notesToSelf}
            onBlur={(e) => updateProfile({ notesToSelf: e.target.value })}
          />
        </label>
        <p className="muted">Zainteresowania — wielokrotne, bez rankingu.</p>
        <div className="chip-row">
          {defaultInterests.map((interest) => {
            const on = profile.interests.includes(interest);
            return (
              <button
                key={interest}
                type="button"
                className="chip"
                aria-pressed={on}
                onClick={() => {
                  const next = on
                    ? profile.interests.filter((i) => i !== interest)
                    : [...profile.interests, interest];
                  void updateProfile({ interests: next });
                }}
              >
                {interest}
              </button>
            );
          })}
        </div>
      </section>

      <section className="card stack">
        <h2>Wygląd</h2>
        <div className="chip-row">
          <button
            type="button"
            className="chip"
            aria-pressed={profile.theme === 'dark'}
            onClick={() => {
              applyTheme('dark');
              void updateProfile({ theme: 'dark' });
            }}
          >
            ciemny
          </button>
          <button
            type="button"
            className="chip"
            aria-pressed={profile.theme === 'light'}
            onClick={() => {
              applyTheme('light');
              void updateProfile({ theme: 'light' });
            }}
          >
            jasny
          </button>
        </div>
      </section>

      <section className="card stack">
        <h2>Asystent AI</h2>
        <p className="muted">
          Aplikacja nigdy nie trzyma klucza API. Podaj URL bramki po swojej stronie (np. funkcja
          serwerowa), która doda sekret i zwróci JSON <code>{'{ "text": "..." }'}</code> albo format
          zgodny z OpenAI chat completions.
        </p>
        <label>
          URL bramki
          <input
            className="field"
            value={gatewayValue}
            onChange={(e) => setGateway(e.target.value)}
            onBlur={() => updateProfile({ aiConfig: { gatewayUrl: gatewayValue.trim(), model: modelValue || undefined } })}
            placeholder="https://twoja-bramka.example/ai"
          />
        </label>
        <label>
          Nazwa modelu (opcjonalnie, przekazywana do bramki)
          <input
            className="field"
            value={modelValue}
            onChange={(e) => setModel(e.target.value)}
            onBlur={() => updateProfile({ aiConfig: { gatewayUrl: gatewayValue.trim(), model: modelValue || undefined } })}
          />
        </label>
        <label>
          Domyślny tryb
          <select
            className="field"
            value={profile.aiDefaultMode}
            onChange={(e) => updateProfile({ aiDefaultMode: e.target.value as AiMode })}
          >
            {AI_MODES.map((mode) => (
              <option key={mode} value={mode}>
                {aiModeLabels[mode]}
              </option>
            ))}
          </select>
        </label>
      </section>

      <section className="card stack">
        <h2>Eksport i import</h2>
        <p className="muted">
          Format JSON <code>neurotrino-lab</code> v1. Załączniki obrazów nie wchodzą do pliku —
          zostają w IndexedDB.
        </p>
        <div className="actions">
          <button
            className="btn primary"
            type="button"
            onClick={async () => {
              const data = await buildExport();
              const blob = new Blob([exportToJson(data)], { type: 'application/json' });
              const url = URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `neurotrino-lab-${data.exportedAt.slice(0, 10)}.json`;
              a.click();
              URL.revokeObjectURL(url);
            }}
          >
            Eksportuj JSON
          </button>
          <button className="btn" type="button" onClick={() => fileRef.current?.click()}>
            Importuj…
          </button>
          <input
            ref={fileRef}
            type="file"
            accept="application/json"
            hidden
            onChange={async (e) => {
              const file = e.target.files?.[0];
              e.target.value = '';
              if (!file) return;
              try {
                const raw = JSON.parse(await file.text()) as unknown;
                const replace = confirm(
                  'OK = zastąp wszystkie dane (najpierw powstanie kopia w bazie). Anuluj = scal.',
                );
                const result = await importLabData(raw, replace ? 'replace' : 'merge');
                setMessage(`Zaimportowano ${result.thoughts} myśli i ${result.projects} projektów.`);
              } catch (error) {
                setMessage(error instanceof Error ? error.message : 'Import nieudany.');
              }
            }}
          />
        </div>
        {message ? <p className="notice">{message}</p> : null}
      </section>

      <section className="card stack">
        <h2>Dane na tym urządzeniu</h2>
        <button className="btn" type="button" onClick={() => removeSampleContent()}>
          Usuń treści przykładowe
        </button>
        <button
          className="btn danger"
          type="button"
          onClick={async () => {
            if (!confirm('Skasować wszystkie lokalne dane i wrócić do stanu początkowego?')) return;
            await resetLocalData();
          }}
        >
          Wyczyść laboratorium
        </button>
      </section>
    </main>
  );
}
