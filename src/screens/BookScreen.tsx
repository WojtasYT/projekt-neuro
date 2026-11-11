import { Link, useNavigate, useParams } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { ThoughtCard } from '../components/ThoughtCard';
import { db } from '../db/database';
import { readingStatusLabels } from '../domain/labels';
import { READING_STATUSES } from '../domain/types';
import { deleteReadingItem, updateReadingItem } from '../services/library';
import { createThought } from '../services/thoughts';

export function BookScreen() {
  const { id } = useParams();
  const navigate = useNavigate();
  const book = useLiveQuery(() => (id ? db.readingItems.get(id) : undefined), [id]);
  const thoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];

  if (!id) return null;
  if (book === undefined) {
    return (
      <main className="page">
        <p className="muted">Otwieram książkę…</p>
      </main>
    );
  }
  if (!book) {
    return (
      <main className="page">
        <div className="notice">Nie znaleziono pozycji.</div>
      </main>
    );
  }

  const linked = thoughts.filter((t) => t.readingItemIds.includes(book.id));

  return (
    <main className="page stack">
      <Link to="/biblioteka" className="muted">
        ← biblioteka
      </Link>
      <h1>{book.title}</h1>
      <p className="muted">{book.author}</p>

      <label>
        Stan lektury
        <select
          className="field"
          value={book.status}
          onChange={(e) => updateReadingItem(book.id, { status: e.target.value as typeof book.status })}
        >
          {READING_STATUSES.map((status) => (
            <option key={status} value={status}>
              {readingStatusLabels[status]}
            </option>
          ))}
        </select>
      </label>
      <label>
        Gdzie jesteś (strona, rozdział — jeśli chcesz)
        <input
          className="field"
          defaultValue={book.currentPage}
          onBlur={(e) => updateReadingItem(book.id, { currentPage: e.target.value })}
        />
      </label>
      <label>
        Notatka o postępie
        <textarea
          className="field"
          rows={2}
          defaultValue={book.progressNote}
          onBlur={(e) => updateReadingItem(book.id, { progressNote: e.target.value })}
        />
      </label>
      <label>
        Własne uwagi
        <textarea
          className="field"
          rows={5}
          defaultValue={book.notes}
          onBlur={(e) => updateReadingItem(book.id, { notes: e.target.value })}
        />
      </label>

      <button
        className="btn primary"
        type="button"
        onClick={async () => {
          const thought = await createThought({
            text: `Notatka z lektury: ${book.title}`,
            types: ['reading_note'],
            readingItemId: book.id,
          });
          navigate(`/mysli/${thought.id}`);
        }}
      >
        Zapisz myśl z tej książki
      </button>

      <div className="list">
        {linked.map((thought) => (
          <ThoughtCard key={thought.id} thought={thought} />
        ))}
      </div>

      {!book.isStarter ? (
        <button
          className="btn danger"
          type="button"
          onClick={async () => {
            await deleteReadingItem(book.id);
            navigate('/biblioteka');
          }}
        >
          Usuń z półki
        </button>
      ) : (
        <p className="muted">Pozycja startowa — możesz zmienić stan, nie musisz jej czytać.</p>
      )}
    </main>
  );
}
