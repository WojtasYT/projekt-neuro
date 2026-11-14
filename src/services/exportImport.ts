import { db, defaultProfile } from '../db/database';
import { parseLabExport } from '../domain/exportSchema';
import { EXPORT_FORMAT, EXPORT_VERSION, type LabExport } from '../domain/types';
import { nowIso } from './ids';

export async function buildExport(): Promise<LabExport> {
  const [thoughts, projects, relationships, sources, readingItems, sparks, profile] =
    await Promise.all([
      db.thoughts.toArray(),
      db.projects.toArray(),
      db.relationships.toArray(),
      db.sources.toArray(),
      db.readingItems.toArray(),
      db.sparks.toArray(),
      db.profile.get('local'),
    ]);
  const safeProfile = profile ?? defaultProfile();
  return {
    format: EXPORT_FORMAT,
    version: EXPORT_VERSION,
    exportedAt: nowIso(),
    thoughts,
    projects,
    relationships,
    sources,
    readingItems,
    sparks,
    profile: safeProfile,
  };
}

export function exportToJson(data: LabExport): string {
  return `${JSON.stringify(data, null, 2)}\n`;
}

export async function backupCurrent(label = 'pre-import'): Promise<void> {
  const data = await buildExport();
  await db.meta.put({
    key: `backup:${label}:${data.exportedAt}`,
    value: data,
  });
  await db.meta.put({ key: 'lastBackupAt', value: data.exportedAt });
}

export async function importLabData(
  raw: unknown,
  mode: 'replace' | 'merge',
): Promise<{ thoughts: number; projects: number }> {
  const data = parseLabExport(raw);
  await backupCurrent();

  if (mode === 'replace') {
    await db.transaction(
      'rw',
      [
        db.thoughts,
        db.projects,
        db.relationships,
        db.sources,
        db.readingItems,
        db.sparks,
        db.profile,
      ],
      async () => {
        await db.thoughts.clear();
        await db.projects.clear();
        await db.relationships.clear();
        await db.sources.clear();
        await db.readingItems.clear();
        await db.sparks.clear();
        await db.thoughts.bulkPut(data.thoughts);
        await db.projects.bulkPut(data.projects);
        await db.relationships.bulkPut(data.relationships);
        await db.sources.bulkPut(data.sources);
        await db.readingItems.bulkPut(data.readingItems);
        await db.sparks.bulkPut(data.sparks);
        await db.profile.put(data.profile);
      },
    );
  } else {
    await db.transaction(
      'rw',
      [
        db.thoughts,
        db.projects,
        db.relationships,
        db.sources,
        db.readingItems,
        db.sparks,
        db.profile,
      ],
      async () => {
        await db.thoughts.bulkPut(data.thoughts);
        await db.projects.bulkPut(data.projects);
        await db.relationships.bulkPut(data.relationships);
        await db.sources.bulkPut(data.sources);
        await db.readingItems.bulkPut(data.readingItems);
        await db.sparks.bulkPut(data.sparks);
        const current = await db.profile.get('local');
        await db.profile.put({
          ...(current ?? data.profile),
          ...data.profile,
          id: 'local',
        });
      },
    );
  }

  return { thoughts: data.thoughts.length, projects: data.projects.length };
}
