import { useMemo, useState } from 'react';
import { useLiveQuery } from 'dexie-react-hooks';
import { EmptyState, ThoughtCard } from '../components/ThoughtCard';
import { useCapture } from '../components/CaptureSheet';
import { db } from '../db/database';
import { thoughtStatusLabels, thoughtTypeLabels } from '../domain/labels';
import type { ThoughtStatus, ThoughtType } from '../domain/types';
import { THOUGHT_STATUSES, THOUGHT_TYPES } from '../domain/types';
import { searchThoughts } from '../services/search';

export function ThoughtsScreen() {
  const capture = useCapture();
  const thoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState<ThoughtStatus | 'all'>('all');
  const [type, setType] = useState<ThoughtType | 'all'>('all');
  const [showArchived, setShowArchived] = useState(false);

  const filtered = useMemo(() => {
    let list = searchThoughts(thoughts, query);
    if (!showArchived) list = list.filter((t) => t.status !== 'archived');
    if (status !== 'all') list = list.filter((t) => t.status === status);
    if (type !== 'all') list = list.filter((t) => t.types.includes(type));
    return [...list].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  }, [thoughts, query, status, type, showArchived]);

  return (
    <main className="page">
      <section className="hero">
        <p className="kicker">Thought Studio</p>
        <h1>Myśli</h1>
        <p className="lede">Szukaj, wracaj, zostawiaj niedokończone. Nic nie musi mieć tytułu.</p>
      </section>

      <input
        className="search-input"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Szukaj w treściach, tagach, hipotezach…"
        aria-label="Szukaj myśli"
      />

      <div className="chip-row" style={{ margin: '12px 0' }}>
        <select
          className="field"
          style={{ width: 'auto' }}
          value={status}
          onChange={(e) => setStatus(e.target.value as ThoughtStatus | 'all')}
        >
          <option value="all">wszystkie stany</option>
          {THOUGHT_STATUSES.map((item) => (
            <option key={item} value={item}>
              {thoughtStatusLabels[item]}
            </option>
          ))}
        </select>
        <select
          className="field"
          style={{ width: 'auto' }}
          value={type}
          onChange={(e) => setType(e.target.value as ThoughtType | 'all')}
        >
          <option value="all">wszystkie typy</option>
          {THOUGHT_TYPES.map((item) => (
            <option key={item} value={item}>
              {thoughtTypeLabels[item]}
            </option>
          ))}
        </select>
        <button
          type="button"
          className="chip"
          aria-pressed={showArchived}
          onClick={() => setShowArchived((v) => !v)}
        >
          archiwum
        </button>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="Brak myśli w tym widoku"
          body="Możesz zapisać strzęp bez kategorii. Filtrowanie jest po to, żeby wracać, nie żeby zaczynać."
          action={
            <button className="btn primary" type="button" onClick={() => capture.open()}>
              Zapisz myśl
            </button>
          }
        />
      ) : (
        <div className="list">
          {filtered.map((thought) => (
            <ThoughtCard key={thought.id} thought={thought} />
          ))}
        </div>
      )}
    </main>
  );
}
