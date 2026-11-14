import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { db } from '../db/database';
import { readingStatusLabels } from '../domain/labels';
import { createReadingItem } from '../services/library';

export function LibraryScreen() {
  const items = useLiveQuery(() => db.readingItems.toArray(), []) ?? [];
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const sorted = [...items].sort((a, b) => a.title.localeCompare(b.title, 'pl'));

  return (
    <main className="page">
      <section className="hero">
        <p className="kicker">Półka</p>
        <h1>Biblioteka</h1>
        <p className="lede">
          Książki są zasobem, nie obowiązkiem. Nie ma dziennej normy stron. Możesz zapisać jedną
          myśl z lektury i odejść.
        </p>
      </section>

      <section className="well">
        <input className="field" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Tytuł" />
        <input
          className="field"
          style={{ marginTop: 8 }}
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
          placeholder="Autor"
        />
        <div className="actions">
          <button
            className="btn primary"
            type="button"
            disabled={!title.trim()}
            onClick={async () => {
              await createReadingItem({ title, author });
              setTitle('');
              setAuthor('');
            }}
          >
            Dodaj książkę
          </button>
        </div>
      </section>

      <div className="list" style={{ marginTop: 16 }}>
        {sorted.map((item) => (
          <Link key={item.id} className="thought-link" to={`/biblioteka/${item.id}`}>
            <article className="thought-card">
              <div className="row-between">
                <strong>{item.title}</strong>
                <span className="status">{readingStatusLabels[item.status]}</span>
              </div>
              <p>
                {item.author}
                {item.isStarter ? ' · z Twojej półki startowej' : ''}
              </p>
              {item.progressNote ? <p className="muted">{item.progressNote}</p> : null}
            </article>
          </Link>
        ))}
      </div>
    </main>
  );
}
