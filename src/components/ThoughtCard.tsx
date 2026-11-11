import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { provenanceLabels, thoughtStatusLabels, thoughtTypeLabels } from '../domain/labels';
import type { Thought } from '../domain/types';
import { formatWhen } from '../services/ids';

export function ThoughtCard({ thought }: { thought: Thought }) {
  const snippet = (thought.content || thought.rawText).trim();
  return (
    <Link className="thought-link" to={`/mysli/${thought.id}`}>
      <article className="thought-card">
        <div className="row-between">
          <strong>{thought.title || 'Bez tytułu'}</strong>
          <span className="meta">{formatWhen(thought.updatedAt)}</span>
        </div>
        {snippet ? <p>{snippet}</p> : null}
        <div className="chip-row" style={{ marginTop: 10 }}>
          <span className={`status ${thought.isSample ? 'sample' : 'user'}`}>
            {thought.isSample ? provenanceLabels.sample : thoughtStatusLabels[thought.status]}
          </span>
          {thought.types.slice(0, 2).map((type) => (
            <span className="status" key={type}>
              {thoughtTypeLabels[type]}
            </span>
          ))}
        </div>
      </article>
    </Link>
  );
}

export function EmptyState({
  title,
  body,
  action,
}: {
  title: string;
  body: string;
  action?: ReactNode;
}) {
  return (
    <div className="empty card">
      <h3>{title}</h3>
      <p>{body}</p>
      {action}
    </div>
  );
}
