import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { ThoughtCard } from '../components/ThoughtCard';
import { useCapture } from '../components/CaptureSheet';
import { db } from '../db/database';
import { projectStatusLabels } from '../domain/labels';
import { PROJECT_STATUSES } from '../domain/types';
import { createRelationship } from '../services/relationships';
import { deleteProject, updateProject } from '../services/projects';
import { formatWhen } from '../services/ids';

export function ProjectDetailScreen() {
  const { id } = useParams();
  const navigate = useNavigate();
  const capture = useCapture();
  const project = useLiveQuery(() => (id ? db.projects.get(id) : undefined), [id]);
  const thoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];
  const [linkId, setLinkId] = useState('');

  if (!id) return null;
  if (project === undefined) {
    return (
      <main className="page">
        <p className="muted">Otwieram projekt…</p>
      </main>
    );
  }
  if (!project) {
    return (
      <main className="page">
        <div className="notice">Nie znaleziono projektu.</div>
      </main>
    );
  }

  const linked = thoughts.filter((t) => t.projectIds.includes(project.id));
  const others = thoughts.filter((t) => !t.projectIds.includes(project.id));

  return (
    <main className="page">
      <Link to="/projekty" className="muted">
        ← projekty
      </Link>
      <section className="hero">
        <p className="kicker">{project.isSample ? 'przykład · można usunąć' : 'projekt'}</p>
        <input
          className="field"
          style={{ fontFamily: 'var(--font-display)', fontSize: '1.8rem' }}
          defaultValue={project.title}
          onBlur={(e) => updateProject(project.id, { title: e.target.value }, 'Zmiana tytułu')}
        />
      </section>

      <div className="stack">
        <label>
          Pytanie / cel
          <textarea
            className="field"
            rows={2}
            defaultValue={project.purpose}
            onBlur={(e) => updateProject(project.id, { purpose: e.target.value })}
          />
        </label>
        <label>
          Notatka
          <textarea
            className="field"
            rows={4}
            defaultValue={project.description}
            onBlur={(e) => updateProject(project.id, { description: e.target.value })}
          />
        </label>
        <label>
          Najbliższy mały krok (nie obowiązek)
          <input
            className="field"
            defaultValue={project.nextStep}
            onBlur={(e) => updateProject(project.id, { nextStep: e.target.value }, 'Nowy mały krok')}
          />
        </label>
        <label>
          Stan
          <select
            className="field"
            value={project.status}
            onChange={(e) =>
              updateProject(project.id, { status: e.target.value as typeof project.status }, 'Zmiana stanu')
            }
          >
            {PROJECT_STATUSES.map((status) => (
              <option key={status} value={status}>
                {projectStatusLabels[status]}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="actions" style={{ marginTop: 12 }}>
        <button className="btn primary" type="button" onClick={() => capture.open()}>
          Zapisz myśl do tego projektu
        </button>
      </div>
      <p className="muted">
        Po zapisie otwórz myśl i wybierz ten projekt albo podepnij istniejącą poniżej.
      </p>

      <h2>Myśli w projekcie</h2>
      <div className="list">
        {linked.map((thought) => (
          <ThoughtCard key={thought.id} thought={thought} />
        ))}
      </div>

      <div className="card" style={{ marginTop: 16 }}>
        <h3>Podłącz istniejącą myśl</h3>
        <select className="field" value={linkId} onChange={(e) => setLinkId(e.target.value)}>
          <option value="">wybierz…</option>
          {others.map((t) => (
            <option key={t.id} value={t.id}>
              {t.title}
            </option>
          ))}
        </select>
        <button
          className="btn"
          type="button"
          disabled={!linkId}
          onClick={async () => {
            const thought = thoughts.find((t) => t.id === linkId);
            if (!thought) return;
            await db.thoughts.put({
              ...thought,
              projectIds: [...thought.projectIds, project.id],
              updatedAt: new Date().toISOString(),
            });
            await createRelationship({
              fromId: thought.id,
              toId: project.id,
              fromKind: 'thought',
              toKind: 'project',
              relationType: 'belongs_to_project',
              provenance: 'user',
            });
            setLinkId('');
          }}
        >
          Podłącz
        </button>
      </div>

      <h3 style={{ marginTop: 24 }}>Historia zmian</h3>
      <ul className="muted">
        {project.history.map((item) => (
          <li key={item.at + item.note}>
            {formatWhen(item.at)} — {item.note}
          </li>
        ))}
      </ul>

      <button
        className="btn danger"
        type="button"
        onClick={async () => {
          if (!confirm('Usunąć projekt? Myśli zostaną, tylko odłączone.')) return;
          await deleteProject(project.id);
          navigate('/projekty');
        }}
      >
        Usuń projekt
      </button>
    </main>
  );
}
