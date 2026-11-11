import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { db } from '../db/database';
import { relationTypeHints, relationTypeLabels } from '../domain/labels';
import { RELATION_TYPES, type RelationType } from '../domain/types';
import { deleteRelationship } from '../services/relationships';

interface NodeView {
  id: string;
  label: string;
  kind: 'thought' | 'project' | 'reading';
  href: string;
}

export function GraphScreen() {
  const thoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];
  const projects = useLiveQuery(() => db.projects.toArray(), []) ?? [];
  const reading = useLiveQuery(() => db.readingItems.toArray(), []) ?? [];
  const rels = useLiveQuery(() => db.relationships.toArray(), []) ?? [];
  const [focus, setFocus] = useState<string>();
  const [filter, setFilter] = useState<RelationType | 'all'>('all');

  const nodes: NodeView[] = useMemo(
    () => [
      ...thoughts
        .filter((t) => t.status !== 'archived')
        .map((t) => ({ id: t.id, label: t.title, kind: 'thought' as const, href: `/mysli/${t.id}` })),
      ...projects.map((p) => ({
        id: p.id,
        label: p.title,
        kind: 'project' as const,
        href: `/projekty/${p.id}`,
      })),
      ...reading.map((b) => ({
        id: b.id,
        label: b.title,
        kind: 'reading' as const,
        href: `/biblioteka/${b.id}`,
      })),
    ],
    [thoughts, projects, reading],
  );

  const visibleRels = rels.filter((r) => filter === 'all' || r.relationType === filter);
  const focusedRels = focus
    ? visibleRels.filter((r) => r.fromId === focus || r.toId === focus)
    : visibleRels;

  const layout = useMemo(() => {
    const width = 720;
    const height = 420;
    const placed = nodes.map((node, index) => {
      const angle = (index / Math.max(nodes.length, 1)) * Math.PI * 2;
      const radius = node.kind === 'thought' ? 150 : node.kind === 'project' ? 90 : 200;
      return {
        ...node,
        x: width / 2 + Math.cos(angle) * radius,
        y: height / 2 + Math.sin(angle) * radius,
      };
    });
    return { width, height, placed };
  }, [nodes]);

  const nodeMap = new Map(layout.placed.map((n) => [n.id, n]));

  return (
    <main className="page">
      <section className="hero">
        <p className="kicker">Wszechświat powiązań</p>
        <h1>Połączenia</h1>
        <p className="lede">
          Relacja istnieje, gdy ją zapiszesz. AI nic tu nie dorysowuje. Na telefonie wygodniejsza
          jest lista; konstelacja pomaga zobaczyć sąsiedztwo.
        </p>
      </section>

      <div className="chip-row">
        <select
          className="field"
          style={{ width: 'auto' }}
          value={filter}
          onChange={(e) => setFilter(e.target.value as RelationType | 'all')}
        >
          <option value="all">wszystkie relacje</option>
          {RELATION_TYPES.map((type) => (
            <option key={type} value={type}>
              {relationTypeLabels[type]}
            </option>
          ))}
        </select>
        <button className="chip" type="button" onClick={() => setFocus(undefined)} aria-pressed={!focus}>
          całość
        </button>
      </div>

      <svg
        className="constellation"
        viewBox={`0 0 ${layout.width} ${layout.height}`}
        role="img"
        aria-label="Mapa powiązań"
      >
        {focusedRels.map((rel) => {
          const from = nodeMap.get(rel.fromId);
          const to = nodeMap.get(rel.toId);
          if (!from || !to) return null;
          return (
            <line
              key={rel.id}
              className="graph-edge"
              x1={from.x}
              y1={from.y}
              x2={to.x}
              y2={to.y}
            />
          );
        })}
        {layout.placed.map((node) => (
          <g key={node.id} onClick={() => setFocus(node.id)} style={{ cursor: 'pointer' }}>
            <circle
              className="graph-node"
              cx={node.x}
              cy={node.y}
              r={focus === node.id ? 16 : 11}
              stroke={node.kind === 'project' ? '#d7b06a' : node.kind === 'reading' ? '#9bb7ff' : '#7fd3c0'}
            />
            <text className="graph-label" x={node.x + 14} y={node.y + 4}>
              {node.label.slice(0, 28)}
            </text>
          </g>
        ))}
      </svg>

      <h2>Lista relacji</h2>
      <div className="rel-list">
        {focusedRels.length === 0 ? (
          <div className="card muted">
            Brak zapisanych powiązań w tym widoku. Otwórz myśl i nazwij relację — analogia to nie
            mechanizm.
          </div>
        ) : (
          focusedRels.map((rel) => {
            const from = nodes.find((n) => n.id === rel.fromId);
            const to = nodes.find((n) => n.id === rel.toId);
            return (
              <article className="card" key={rel.id}>
                <p className="kicker">{relationTypeLabels[rel.relationType]}</p>
                <p>
                  <Link to={from?.href ?? '#'}>{from?.label ?? rel.fromId}</Link>
                  {' → '}
                  <Link to={to?.href ?? '#'}>{to?.label ?? rel.toId}</Link>
                </p>
                <p className="muted">{rel.explanation || relationTypeHints[rel.relationType]}</p>
                <p className="meta">{rel.provenance === 'sample' ? 'przykład' : 'Twoje powiązanie'}</p>
                <button className="btn ghost" type="button" onClick={() => deleteRelationship(rel.id)}>
                  Usuń relację
                </button>
              </article>
            );
          })
        )}
      </div>
    </main>
  );
}
