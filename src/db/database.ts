import Dexie, { type Table } from 'dexie';
import { SAMPLE_PROJECT, SAMPLE_RELATIONSHIPS, SAMPLE_THOUGHTS } from '../data/sampleContent';
import { STARTER_LIBRARY } from '../data/starterLibrary';
import type {
  AttachmentRecord,
  Project,
  ReadingItem,
  Relationship,
  Source,
  Spark,
  Thought,
  UserProfile,
} from '../domain/types';
import { nowIso } from '../services/ids';

export interface MetaRecord {
  key: string;
  value: unknown;
}

export const defaultProfile = (): UserProfile => ({
  id: 'local',
  displayName: '',
  interests: [
    'neurobiologia',
    'neurochemia',
    'percepcja',
    'kosmologia',
    'inżynieria',
    'filozofia',
  ],
  currentFocus: '',
  onboarded: false,
  theme: 'dark',
  aiDefaultMode: 'iskra',
  aiConfig: { gatewayUrl: '' },
  remindersEnabled: false,
  notesToSelf: '',
  updatedAt: nowIso(),
});

export class NeurotrinoDB extends Dexie {
  thoughts!: Table<Thought, string>;
  projects!: Table<Project, string>;
  relationships!: Table<Relationship, string>;
  sources!: Table<Source, string>;
  readingItems!: Table<ReadingItem, string>;
  sparks!: Table<Spark, string>;
  attachments!: Table<AttachmentRecord, string>;
  profile!: Table<UserProfile, string>;
  meta!: Table<MetaRecord, string>;

  constructor() {
    super('neurotrino-lab');
    this.version(1).stores({
      thoughts: 'id, status, updatedAt, lastOpenedAt, createdAt',
      projects: 'id, status, updatedAt',
      relationships: 'id, fromId, toId, relationType',
      sources: 'id, title',
      readingItems: 'id, status, title',
      sparks: 'id, status, kind',
      attachments: 'id, thoughtId',
      profile: 'id',
      meta: 'key',
    });
  }
}

export const db = new NeurotrinoDB();

export async function ensureSeeded(): Promise<void> {
  await db.open();
  const seeded = await db.meta.get('seeded');
  if (seeded) {
    const profile = await db.profile.get('local');
    if (!profile) await db.profile.put(defaultProfile());
    return;
  }

  const stamp = nowIso();
  const books: ReadingItem[] = STARTER_LIBRARY.map((book) => ({
    ...book,
    createdAt: stamp,
    updatedAt: stamp,
  }));

  await db.transaction(
    'rw',
    [db.thoughts, db.projects, db.relationships, db.readingItems, db.profile, db.meta],
    async () => {
      const already = await db.meta.get('seeded');
      if (already) return;
      await db.profile.put(defaultProfile());
      await db.readingItems.bulkPut(books);
      await db.projects.put(SAMPLE_PROJECT);
      await db.thoughts.bulkPut(SAMPLE_THOUGHTS);
      await db.relationships.bulkPut(SAMPLE_RELATIONSHIPS);
      await db.meta.put({ key: 'seeded', value: true });
    },
  );
}

export async function resetLocalData(): Promise<void> {
  await db.transaction(
    'rw',
    [
      db.thoughts,
      db.projects,
      db.relationships,
      db.sources,
      db.readingItems,
      db.sparks,
      db.attachments,
      db.profile,
      db.meta,
    ],
    async () => {
      await Promise.all([
        db.thoughts.clear(),
        db.projects.clear(),
        db.relationships.clear(),
        db.sources.clear(),
        db.readingItems.clear(),
        db.sparks.clear(),
        db.attachments.clear(),
        db.profile.clear(),
        db.meta.clear(),
      ]);
    },
  );
  await ensureSeeded();
}

export async function removeSampleContent(): Promise<void> {
  const sampleThoughts = await db.thoughts.filter((t) => Boolean(t.isSample)).toArray();
  const sampleProjects = await db.projects.filter((p) => Boolean(p.isSample)).toArray();
  const sampleIds = new Set([
    ...sampleThoughts.map((t) => t.id),
    ...sampleProjects.map((p) => p.id),
  ]);
  await db.transaction('rw', [db.thoughts, db.projects, db.relationships], async () => {
    await db.thoughts.bulkDelete(sampleThoughts.map((t) => t.id));
    await db.projects.bulkDelete(sampleProjects.map((p) => p.id));
    const rels = await db.relationships.toArray();
    const stale = rels.filter((r) => sampleIds.has(r.fromId) || sampleIds.has(r.toId));
    await db.relationships.bulkDelete(stale.map((r) => r.id));
  });
}
