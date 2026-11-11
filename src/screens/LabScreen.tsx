import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { useCapture } from '../components/CaptureSheet';
import { ThoughtCard } from '../components/ThoughtCard';
import { db } from '../db/database';
import { mindStateHints, mindStateLabels } from '../domain/labels';
import type { MindState } from '../domain/types';
import { pickSpark } from '../services/sparks';
import { lastResumableThought, recentThoughts } from '../services/search';
import { setMindState } from '../services/profile';
import { captureThoughts } from '../services/thoughts';

export function LabScreen() {
  const capture = useCapture();
  const navigate = useNavigate();
  const thoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];
  const projects = useLiveQuery(() => db.projects.toArray(), []) ?? [];
  const reading = useLiveQuery(() => db.readingItems.toArray(), []) ?? [];
  const profile = useLiveQuery(() => db.profile.get('local'), []);
  const [draft, setDraft] = useState('');
  const mind = profile?.lastMindState;

  const resume = lastResumableThought(thoughts);
  const recent = recentThoughts(thoughts, 4);
  const activeProject = projects.find((p) => p.status === 'active' || p.status === 'incubating');

  const spark = useMemo(
    () => pickSpark({ thoughts, reading, avoidIds: [] }),
    // Recalculate when data identity changes, not on every keystroke.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [thoughts.length, reading.length, thoughts[0]?.updatedAt],
  );

  async function chooseMind(next: MindState) {
    await setMindState(mind === next ? undefined : next);
  }

  return (
    <main className="page">
      <section className="hero">
        <p className="kicker">Laboratorium</p>
        <h1>Możesz zacząć od jednej myśli.</h1>
        <p className="lede">
          Nic tu nie zalega. Zapis fragmentu jest pełnym użyciem laboratorium — nie musisz dziś
          niczego domykać.
        </p>
      </section>

      <div className="chip-row" style={{ marginBottom: 14 }}>
        {(Object.keys(mindStateLabels) as MindState[]).map((key) => (
          <button
            key={key}
            type="button"
            className="chip"
            aria-pressed={mind === key}
            onClick={() => chooseMind(key)}
          >
            {mindStateLabels[key]}
          </button>
        ))}
      </div>
      {mind ? <p className="muted">{mindStateHints[mind]}</p> : null}

      <section className="well" style={{ marginTop: 16 }}>
        <label htmlFor="lab-capture">Szybki zapis</label>
        <textarea
          id="lab-capture"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          placeholder={mind === 'mgła' ? 'Zastanawia mnie, jak…' : 'Co zauważyłeś, o co pytasz, co nie daje spokoju?'}
        />
        <div className="actions">
          <button
            className="btn primary"
            type="button"
            onClick={async () => {
              if (!draft.trim()) {
                capture.open({ mind });
                return;
              }
              if (mind) await setMindState(mind);
              const created = await captureThoughts({ text: draft });
              setDraft('');
              const first = created[0];
              if (first) navigate(`/mysli/${first.id}`);
            }}
          >
            Zapisz myśl
          </button>
          <button
            className="btn"
            type="button"
            disabled={!resume}
            onClick={() => resume && navigate(`/mysli/${resume.id}`)}
          >
            Wróć do wątku
          </button>
          <button className="btn" type="button" onClick={() => navigate('/eksploracja')}>
            Zaskocz mnie
          </button>
        </div>
      </section>

      <div className="grid-cards">
        {resume ? (
          <article className="card">
            <p className="kicker">Ostatni wątek</p>
            <h2>{resume.title}</h2>
            <p className="muted">Bez zaległości. Możesz tylko spojrzeć albo dopisać jedno zdanie.</p>
            <div className="actions">
              <Link className="btn primary" to={`/mysli/${resume.id}`}>
                Otwórz
              </Link>
            </div>
          </article>
        ) : (
          <article className="card">
            <p className="kicker">Ostatni wątek</p>
            <h2>Tu pojawi się myśl, do której warto wrócić.</h2>
            <p className="muted">Na razie laboratorium jest prawie puste — i to jest w porządku.</p>
          </article>
        )}

        {activeProject ? (
          <article className="card">
            <p className="kicker">Projekt</p>
            <h2>{activeProject.title}</h2>
            <p className="muted">{activeProject.nextStep || activeProject.purpose}</p>
            <div className="actions">
              <Link className="btn" to={`/projekty/${activeProject.id}`}>
                Wejdź
              </Link>
            </div>
          </article>
        ) : null}

        <article className="card">
          <p className="kicker">{spark.kind === 'resume' ? 'Wznowienie' : 'Jedna iskra'}</p>
          <h2>{spark.title}</h2>
          <p className="muted">{spark.hook}</p>
          <div className="actions">
            <Link className="btn" to="/eksploracja">
              Czytaj
            </Link>
          </div>
        </article>
      </div>

      <h2 style={{ marginTop: 28 }}>Niedawno</h2>
      <div className="list">
        {recent.map((thought) => (
          <ThoughtCard key={thought.id} thought={thought} />
        ))}
      </div>
    </main>
  );
}
