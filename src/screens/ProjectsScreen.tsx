import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { EmptyState } from '../components/ThoughtCard';
import { db } from '../db/database';
import { projectStatusLabels } from '../domain/labels';
import { createProject } from '../services/projects';

export function ProjectsScreen() {
  const projects = useLiveQuery(() => db.projects.toArray(), []) ?? [];
  const [title, setTitle] = useState('');
  const [purpose, setPurpose] = useState('');

  const sorted = [...projects].sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));

  return (
    <main className="page">
      <section className="hero">
        <p className="kicker">Inkubator</p>
        <h1>Projekty</h1>
        <p className="lede">
          Długie wątki bez deadline’u. Projekt trzyma myśli, pytanie i najbliższy mały krok — nic
          więcej, jeśli nie chcesz.
        </p>
      </section>

      <section className="well">
        <input
          className="field"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Nazwa projektu"
        />
        <input
          className="field"
          style={{ marginTop: 8 }}
          value={purpose}
          onChange={(e) => setPurpose(e.target.value)}
          placeholder="Aktualne pytanie albo cel (opcjonalnie)"
        />
        <div className="actions">
          <button
            className="btn primary"
            type="button"
            disabled={!title.trim()}
            onClick={async () => {
              await createProject({ title, purpose });
              setTitle('');
              setPurpose('');
            }}
          >
            Utwórz projekt
          </button>
        </div>
      </section>

      <div className="list" style={{ marginTop: 18 }}>
        {sorted.length === 0 ? (
          <EmptyState
            title="Brak projektów"
            body="Projekt pojawia się, gdy myśl chce żyć dłużej niż jedna sesja."
          />
        ) : (
          sorted.map((project) => (
            <Link key={project.id} className="thought-link" to={`/projekty/${project.id}`}>
              <article className="thought-card">
                <div className="row-between">
                  <strong>{project.title}</strong>
                  <span className="status">{projectStatusLabels[project.status]}</span>
                </div>
                <p>{project.purpose || project.nextStep || project.description || 'Bez opisu — to też w porządku.'}</p>
                {project.isSample ? <span className="status sample">przykład</span> : null}
              </article>
            </Link>
          ))
        )}
      </div>
    </main>
  );
}
