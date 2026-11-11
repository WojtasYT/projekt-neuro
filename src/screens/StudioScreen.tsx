import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { db } from '../db/database';
import {
  aiModeHints,
  aiModeLabels,
  provenanceLabels,
  relationTypeLabels,
  thoughtStatusLabels,
  thoughtTypeLabels,
} from '../domain/labels';
import { AI_MODES, RELATION_TYPES, THOUGHT_STATUSES, THOUGHT_TYPES } from '../domain/types';
import type { AiMode, HypothesisFields, RelationType, ThoughtType } from '../domain/types';
import { localImpulseFor } from '../data/localImpulses';
import { useDebounced } from '../hooks/useDebounced';
import { useOnline } from '../hooks/useOnline';
import { isAiConfigured, consentPreview, requestAiImpulse } from '../services/ai';
import {
  addImageAttachment,
  deleteAttachment,
  getAttachmentUrl,
  listAttachmentMeta,
} from '../services/attachments';
import { formatWhen } from '../services/ids';
import { createRelationship, deleteRelationship } from '../services/relationships';
import {
  addExpansion,
  archiveThought,
  deleteThought,
  touchThought,
  updateThought,
} from '../services/thoughts';

const hypoFields: Array<{ key: keyof HypothesisFields; label: string }> = [
  { key: 'observation', label: 'Co zauważyłem?' },
  { key: 'proposal', label: 'Co proponuję?' },
  { key: 'mechanism', label: 'Jaki mechanizm mógłby to tłumaczyć?' },
  { key: 'assumptions', label: 'Które części są założeniami?' },
  { key: 'known', label: 'Co już jest znane?' },
  { key: 'alternatives', label: 'Jakie są inne wyjaśnienia?' },
  { key: 'expectations', label: 'Czego bym się spodziewał, gdyby to było prawdziwe?' },
  { key: 'falsifiers', label: 'Co by to osłabiło albo obaliło?' },
  { key: 'nextStep', label: 'Jaki bezpieczny następny krok?' },
  { key: 'related', label: 'Jakie źródła albo wcześniejsze myśli są blisko?' },
];

export function StudioScreen() {
  const { id } = useParams();
  const navigate = useNavigate();
  const online = useOnline();
  const thought = useLiveQuery(async () => {
    if (!id) return null;
    const row = await db.thoughts.get(id);
    return row ?? null;
  }, [id]);
  const projects = useLiveQuery(() => db.projects.toArray(), []) ?? [];
  const allThoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];
  const rels = useLiveQuery(() => db.relationships.toArray(), []) ?? [];
  const profile = useLiveQuery(() => db.profile.get('local'), []);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [save, setSave] = useState<'idle' | 'saving' | 'saved'>('idle');
  const [showHypo, setShowHypo] = useState(false);
  const [showRaw, setShowRaw] = useState(false);
  const [expansion, setExpansion] = useState('');
  const [relTarget, setRelTarget] = useState('');
  const [relType, setRelType] = useState<RelationType>('extends');
  const [relNote, setRelNote] = useState('');
  const [tagDraft, setTagDraft] = useState('');
  const [aiMode, setAiMode] = useState<AiMode>('iskra');
  const [aiConsent, setAiConsent] = useState(false);
  const [aiText, setAiText] = useState<string>();
  const [aiError, setAiError] = useState<string>();
  const [aiBusy, setAiBusy] = useState(false);
  const [images, setImages] = useState<Array<{ id: string; url: string; name: string }>>([]);

  useEffect(() => {
    if (!thought) return;
    setTitle(thought.title);
    setContent(thought.content);
    setShowHypo(Boolean(thought.hypothesis && Object.values(thought.hypothesis).some(Boolean)));
    setAiMode(profile?.aiDefaultMode ?? 'iskra');
    void touchThought(thought.id);
  }, [thought?.id, profile?.aiDefaultMode]);

  useEffect(() => {
    if (!id) return;
    const urls: string[] = [];
    void listAttachmentMeta(id).then(async (metas) => {
      const loaded = [];
      for (const meta of metas) {
        const url = await getAttachmentUrl(meta.id);
        if (url) {
          urls.push(url);
          loaded.push({ id: meta.id, url, name: meta.name });
        }
      }
      setImages(loaded);
    });
    return () => urls.forEach((url) => URL.revokeObjectURL(url));
  }, [id, thought?.updatedAt]);

  const debouncedTitle = useDebounced(title);
  const debouncedContent = useDebounced(content);

  useEffect(() => {
    if (!thought) return;
    if (debouncedTitle === thought.title && debouncedContent === thought.content) return;
    setSave('saving');
    void updateThought(thought.id, { title: debouncedTitle, content: debouncedContent }).then(() =>
      setSave('saved'),
    );
  }, [debouncedTitle, debouncedContent, thought]);

  const linked = useMemo(() => {
    if (!thought) return [];
    return rels.filter((r) => r.fromId === thought.id || r.toId === thought.id);
  }, [rels, thought]);

  if (!id) return null;
  if (thought === undefined) {
    return (
      <main className="page">
        <p className="muted">Otwieram myśl…</p>
      </main>
    );
  }
  if (thought === null) {
    return (
      <main className="page">
        <div className="notice">Nie znaleziono tej myśli. Mogła zostać usunięta.</div>
        <Link to="/mysli">Wróć do listy</Link>
      </main>
    );
  }

  const current = thought;
  const impulse = localImpulseFor({
    types: current.types,
    status: current.status,
    hasHypothesis: Boolean(current.hypothesis),
  });
  const aiReady = isAiConfigured(profile);

  async function toggleType(type: ThoughtType) {
    const next = current.types.includes(type)
      ? current.types.filter((item) => item !== type)
      : [...current.types, type];
    await updateThought(current.id, { types: next });
  }

  async function addTag() {
    const tag = tagDraft.trim().toLowerCase();
    if (!tag) return;
    if (current.tags.includes(tag)) return;
    await updateThought(current.id, { tags: [...current.tags, tag] });
    setTagDraft('');
  }

  async function saveHypo(key: keyof HypothesisFields, value: string) {
    await updateThought(current.id, {
      hypothesis: { ...(current.hypothesis ?? {}), [key]: value },
    });
  }

  async function onAttach(file: File | undefined) {
    if (!file) return;
    await addImageAttachment(current.id, file);
    await updateThought(current.id, {});
  }

  return (
    <main className="page studio">
      <section className="studio-canvas card">
        <div className="row-between">
          <Link to="/mysli" className="muted">
            ← myśli
          </Link>
          <span className="save-dot">
            {save === 'saving' ? 'zapisywanie…' : save === 'saved' ? 'zapisano lokalnie' : 'autosave'}
          </span>
        </div>
        <input
          className="field"
          style={{ margin: '12px 0', fontFamily: 'var(--font-display)', fontSize: '1.5rem' }}
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Tytuł — opcjonalny"
        />
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Pisz swobodnie. Surowy pierwszy zapis zostaje osobno."
        />
        <p className="meta">utworzono {formatWhen(thought.createdAt)}</p>

        <div className="chip-row" style={{ marginTop: 12 }}>
          {THOUGHT_TYPES.map((type) => (
            <button
              key={type}
              type="button"
              className="chip"
              aria-pressed={thought.types.includes(type)}
              onClick={() => toggleType(type)}
            >
              {thoughtTypeLabels[type]}
            </button>
          ))}
        </div>

        <div className="row-between" style={{ marginTop: 14 }}>
          <label>
            Stan
            <select
              className="field"
              value={thought.status}
              onChange={(e) => updateThought(thought.id, { status: e.target.value as typeof thought.status })}
            >
              {THOUGHT_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {thoughtStatusLabels[status]}
                </option>
              ))}
            </select>
          </label>
          <label>
            Projekt
            <select
              className="field"
              value={thought.projectIds[0] ?? ''}
              onChange={(e) =>
                updateThought(thought.id, { projectIds: e.target.value ? [e.target.value] : [] })
              }
            >
              <option value="">bez projektu</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.title}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="chip-row" style={{ marginTop: 12 }}>
          {thought.tags.map((tag) => (
            <button
              key={tag}
              type="button"
              className="chip"
              onClick={() =>
                updateThought(thought.id, { tags: thought.tags.filter((item) => item !== tag) })
              }
            >
              #{tag} ×
            </button>
          ))}
          <input
            className="field"
            style={{ width: 160 }}
            value={tagDraft}
            placeholder="tag"
            onChange={(e) => setTagDraft(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                void addTag();
              }
            }}
          />
        </div>

        <hr className="sep" />
        <button className="btn ghost" type="button" onClick={() => setShowRaw((v) => !v)}>
          {showRaw ? 'Ukryj surowy zapis' : 'Pokaż surowy zapis'}
        </button>
        {showRaw ? (
          <div className="raw-box">
            <p className="kicker">{provenanceLabels[thought.provenance]} · oryginał</p>
            {thought.rawText || '—'}
          </div>
        ) : null}

        <hr className="sep" />
        <button className="btn" type="button" onClick={() => setShowHypo((v) => !v)}>
          {showHypo ? 'Zwiń kartę hipotezy' : 'Karta hipotezy (opcjonalna)'}
        </button>
        {showHypo ? (
          <div className="hypo-grid" style={{ marginTop: 12 }}>
            <p className="muted">Żadne pole nie jest obowiązkowe. Puste mogą tak zostać.</p>
            {hypoFields.map((field) => (
              <label key={field.key}>
                {field.label}
                <textarea
                  className="field"
                  rows={2}
                  defaultValue={thought.hypothesis?.[field.key] ?? ''}
                  onBlur={(e) => saveHypo(field.key, e.target.value)}
                />
              </label>
            ))}
          </div>
        ) : null}

        <hr className="sep" />
        <h3>Rozwinięcia</h3>
        <p className="muted">Osobne dopiski — nie nadpisują oryginału.</p>
        {thought.expansions.map((item) => (
          <div className="raw-box" key={item.id} style={{ marginTop: 8 }}>
            <p className="kicker">
              {provenanceLabels[item.provenance]} · {formatWhen(item.createdAt)}
            </p>
            {item.text}
          </div>
        ))}
        <textarea
          className="field"
          rows={3}
          value={expansion}
          onChange={(e) => setExpansion(e.target.value)}
          placeholder="Dopisz warstwę, skojarzenie, zastrzeżenie…"
        />
        <div className="actions">
          <button
            className="btn"
            type="button"
            disabled={!expansion.trim()}
            onClick={async () => {
              await addExpansion(thought.id, expansion, 'user');
              setExpansion('');
            }}
          >
            Dodaj jako Twoje
          </button>
        </div>

        <hr className="sep" />
        <h3>Obrazy i szkice</h3>
        <input
          type="file"
          accept="image/*"
          onChange={(e) => {
            void onAttach(e.target.files?.[0]);
            e.target.value = '';
          }}
        />
        <div className="attach-grid" style={{ marginTop: 10 }}>
          {images.map((img) => (
            <figure key={img.id} style={{ margin: 0 }}>
              <img src={img.url} alt={img.name} />
              <button className="btn ghost" type="button" onClick={() => deleteAttachment(img.id)}>
                usuń
              </button>
            </figure>
          ))}
        </div>

        <hr className="sep" />
        <div className="actions">
          <button className="btn" type="button" onClick={() => archiveThought(thought.id)}>
            Archiwizuj
          </button>
          <button
            className="btn danger"
            type="button"
            onClick={async () => {
              if (!confirm('Usunąć tę myśl trwale z urządzenia?')) return;
              await deleteThought(thought.id);
              navigate('/mysli');
            }}
          >
            Usuń
          </button>
        </div>
      </section>

      <aside className="side-panel">
        <section className="card">
          <p className="kicker">Impuls lokalny</p>
          <p>{impulse}</p>
          <p className="muted">To nie jest AI. Jedno pytanie z katalogu, działa offline.</p>
        </section>

        <section className="card">
          <p className="kicker">Asystent</p>
          {!aiReady ? (
            <p className="notice">
              Asystent nie jest skonfigurowany. Nie udaję odpowiedzi. W ustawieniach możesz podać
              adres własnej bramki po stronie serwera — klucz API nigdy nie powinien trafić do tej
              aplikacji.
            </p>
          ) : !online ? (
            <p className="notice">AI wymaga sieci. Jesteś offline.</p>
          ) : (
            <>
              <label>
                Tryb
                <select
                  className="field"
                  value={aiMode}
                  onChange={(e) => setAiMode(e.target.value as AiMode)}
                >
                  {AI_MODES.map((mode) => (
                    <option key={mode} value={mode}>
                      {aiModeLabels[mode]}
                    </option>
                  ))}
                </select>
              </label>
              <p className="muted">{aiModeHints[aiMode]}</p>
              <p className="muted">Zostanie wysłane:</p>
              <div className="raw-box">{consentPreview(thought)}</div>
              <label className="muted">
                <input
                  type="checkbox"
                  checked={aiConsent}
                  onChange={(e) => setAiConsent(e.target.checked)}
                />{' '}
                Rozumiem, że ten fragment opuści urządzenie.
              </label>
              <button
                className="btn primary"
                type="button"
                disabled={!aiConsent || aiBusy}
                onClick={async () => {
                  if (!profile) return;
                  setAiBusy(true);
                  setAiError(undefined);
                  try {
                    const result = await requestAiImpulse({ mode: aiMode, thought }, profile);
                    setAiText(result.text);
                  } catch (error) {
                    setAiText(undefined);
                    setAiError(error instanceof Error ? error.message : 'Nie udało się.');
                  } finally {
                    setAiBusy(false);
                  }
                }}
              >
                {aiBusy ? 'czekam…' : 'Poproś o impuls'}
              </button>
              {aiError ? <p className="notice">{aiError}</p> : null}
              {aiText ? (
                <div className="raw-box">
                  <p className="kicker status ai">AI · {aiModeLabels[aiMode]}</p>
                  {aiText}
                  <div className="actions">
                    <button
                      className="btn"
                      type="button"
                      onClick={async () => {
                        await addExpansion(thought.id, aiText, 'ai');
                        setAiText(undefined);
                      }}
                    >
                      Zachowaj jako sugestię AI
                    </button>
                  </div>
                </div>
              ) : null}
            </>
          )}
        </section>

        <section className="card">
          <p className="kicker">Powiązania</p>
          <div className="rel-list">
            {linked.map((rel) => {
              const otherId = rel.fromId === thought.id ? rel.toId : rel.fromId;
              const other = allThoughts.find((t) => t.id === otherId);
              const project = projects.find((p) => p.id === otherId);
              const label = other?.title || project?.title || otherId;
              const to = other ? `/mysli/${other.id}` : project ? `/projekty/${project.id}` : '/polaczenia';
              return (
                <div key={rel.id} className="row-between">
                  <Link to={to}>
                    {relationTypeLabels[rel.relationType]} → {label}
                  </Link>
                  <button className="btn ghost" type="button" onClick={() => deleteRelationship(rel.id)}>
                    ×
                  </button>
                </div>
              );
            })}
          </div>
          <label>
            Połącz z inną myślą
            <select className="field" value={relTarget} onChange={(e) => setRelTarget(e.target.value)}>
              <option value="">wybierz…</option>
              {allThoughts
                .filter((t) => t.id !== thought.id)
                .map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.title}
                  </option>
                ))}
            </select>
          </label>
          <select className="field" value={relType} onChange={(e) => setRelType(e.target.value as RelationType)}>
            {RELATION_TYPES.map((type) => (
              <option key={type} value={type}>
                {relationTypeLabels[type]}
              </option>
            ))}
          </select>
          <input
            className="field"
            value={relNote}
            onChange={(e) => setRelNote(e.target.value)}
            placeholder="Dlaczego ta relacja? (opcjonalnie)"
          />
          <button
            className="btn"
            type="button"
            disabled={!relTarget}
            onClick={async () => {
              await createRelationship({
                fromId: thought.id,
                toId: relTarget,
                fromKind: 'thought',
                toKind: 'thought',
                relationType: relType,
                explanation: relNote,
                provenance: 'user',
              });
              if (thought.status === 'seed') await updateThought(thought.id, { status: 'connected' });
              setRelTarget('');
              setRelNote('');
            }}
          >
            Dodaj powiązanie
          </button>
        </section>
      </aside>
    </main>
  );
}
