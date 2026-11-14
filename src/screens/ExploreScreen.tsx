import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLiveQuery } from 'dexie-react-hooks';
import { db } from '../db/database';
import { CATALOG_SPARKS } from '../data/sparks';
import type { Spark } from '../domain/types';
import { pickSpark, sparkToThoughtBody } from '../services/sparks';
import { createThought } from '../services/thoughts';

export function ExploreScreen() {
  const navigate = useNavigate();
  const thoughts = useLiveQuery(() => db.thoughts.toArray(), []) ?? [];
  const reading = useLiveQuery(() => db.readingItems.toArray(), []) ?? [];
  const [avoid, setAvoid] = useState<string[]>([]);
  const [current, setCurrent] = useState<Spark | null>(null);
  const [deeper, setDeeper] = useState(false);

  const spark = useMemo(() => {
    if (current) return current;
    return pickSpark({ thoughts, reading, avoidIds: avoid });
  }, [current, thoughts, reading, avoid]);

  function another() {
    setDeeper(false);
    setAvoid((ids) => [...ids, spark.id]);
    setCurrent(pickSpark({ thoughts, reading, avoidIds: [...avoid, spark.id] }));
  }

  return (
    <main className="page">
      <section className="hero">
        <p className="kicker">Eksploracja</p>
        <h1>Jedna iskra, nie feed.</h1>
        <p className="lede">
          To lokalny katalog i Twoje własne wątki — nie live AI. Źródła i otwarte pytania są
          nazwane; spekulacja nie udaje odkrycia.
        </p>
      </section>

      <article className="well">
        <p className="kicker">
          {spark.generatedBy === 'catalog' ? 'iskra z katalogu' : 'iskra z Twoich zapisów'}
        </p>
        <h2>{spark.title}</h2>
        <p>{spark.hook}</p>
        <p className="muted">{spark.explanation}</p>
        {deeper && spark.deeper ? <p>{spark.deeper}</p> : null}
        <p>
          <strong>Pytanie: </strong>
          {spark.question}
        </p>
        {spark.sourceNote ? <p className="meta">{spark.sourceNote}</p> : null}
        <div className="chip-row">
          {spark.domains.map((d) => (
            <span className="status" key={d}>
              {d}
            </span>
          ))}
        </div>
        <div className="actions">
          <button
            className="btn primary"
            type="button"
            onClick={async () => {
              const thought = await createThought({
                text: sparkToThoughtBody(spark),
                types: spark.kind === 'open_question' ? ['question'] : ['observation'],
              });
              if (spark.linkedThoughtIds[0]) {
                navigate(`/mysli/${spark.linkedThoughtIds[0]}`);
                return;
              }
              navigate(`/mysli/${thought.id}`);
            }}
          >
            Zapisz jako myśl
          </button>
          {spark.deeper ? (
            <button className="btn" type="button" onClick={() => setDeeper((v) => !v)}>
              {deeper ? 'Mniej' : 'Głębiej'}
            </button>
          ) : null}
          <button className="btn" type="button" onClick={another}>
            Inny kąt
          </button>
        </div>
      </article>

      <h2 style={{ marginTop: 28 }}>Katalog (nie nieskończony)</h2>
      <div className="list">
        {CATALOG_SPARKS.map((item) => (
          <button
            key={item.id}
            type="button"
            className="thought-card"
            style={{ textAlign: 'left', width: '100%' }}
            onClick={() => {
              setCurrent(item);
              setDeeper(false);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
          >
            <strong>{item.title}</strong>
            <p>{item.hook}</p>
          </button>
        ))}
      </div>
    </main>
  );
}
