import { CATALOG_SPARKS } from '../data/sparks';
import type { ReadingItem, Spark, Thought } from '../domain/types';
import { createId } from './ids';

export function pickSpark(args: {
  thoughts: Thought[];
  reading: ReadingItem[];
  avoidIds?: string[];
}): Spark {
  const avoid = new Set(args.avoidIds ?? []);
  const resume = [...args.thoughts]
    .filter((t) => t.status === 'paused' || t.status === 'exploring' || t.status === 'seed')
    .sort((a, b) => a.updatedAt.localeCompare(b.updatedAt))[0];

  const resumeSpark: Spark | undefined = resume
    ? {
        id: createId('spark'),
        title: 'Wątek, który czeka bez presji',
        hook: resume.title || 'Niedokończona myśl',
        explanation: clip(resume.content || resume.rawText, 360),
        question: 'Jaki jeden szczegół z tej notatki wciąż jest ciekawy — nawet jeśli reszta ostygła?',
        domains: resume.tags.slice(0, 3),
        kind: 'resume',
        generatedBy: 'local',
        status: 'fresh',
        linkedThoughtIds: [resume.id],
      }
    : undefined;

  const readingSpark = readingSparkFrom(args.reading);

  const catalog = CATALOG_SPARKS.filter((spark) => !avoid.has(spark.id));
  const pool: Spark[] = [...catalog];
  if (resumeSpark) pool.push(resumeSpark);
  if (readingSpark) pool.push(readingSpark);

  if (!pool.length) return CATALOG_SPARKS[0]!;
  const index = Math.floor(Math.random() * pool.length);
  return pool[index]!;
}

function readingSparkFrom(reading: ReadingItem[]): Spark | undefined {
  const open = reading.find((item) => item.status === 'exploring' || item.status === 'paused');
  if (!open) return undefined;
  return {
    id: createId('spark'),
    title: `Książka jako iskra: ${open.title}`,
    hook: open.progressNote || 'Nie musisz czytać rozdziału. Wystarczy jeden własny fragment albo pytanie.',
    explanation: `${open.author} — ${open.title}. Lektura jest zasobem, nie obowiązkiem.`,
    question: 'Jaki jeden obraz albo zdanie z tej książki (jeśli pamiętasz) łączy się z czymś, co ostatnio zauważyłeś?',
    domains: ['lektura'],
    kind: 'reading',
    generatedBy: 'local',
    status: 'fresh',
    linkedThoughtIds: [],
  };
}

function clip(text: string, max: number): string {
  const t = text.trim();
  return t.length > max ? `${t.slice(0, max - 1)}…` : t;
}

export function sparkToThoughtBody(spark: Spark): string {
  return [spark.hook, '', spark.explanation, spark.deeper ? `\nGłębiej: ${spark.deeper}` : '', '', `Pytanie: ${spark.question}`]
    .filter((part) => part !== undefined)
    .join('\n')
    .trim();
}
