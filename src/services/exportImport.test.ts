import { afterEach, describe, expect, it } from 'vitest';
import { db, ensureSeeded } from '../db/database';
import { captureThoughts } from './thoughts';
import { buildExport, importLabData } from './exportImport';

describe('export/import', () => {
  afterEach(async () => {
    await db.delete();
  });

  it('round-trips a user thought', async () => {
    await ensureSeeded();
    await captureThoughts({ text: 'Foton kończy się na opsynie.' });
    const exported = await buildExport();
    expect(exported.thoughts.some((t) => t.rawText.includes('opsynie'))).toBe(true);

    await db.delete();
    await db.open();
    await importLabData(exported, 'replace');
    const thoughts = await db.thoughts.toArray();
    expect(thoughts.some((t) => t.rawText.includes('opsynie'))).toBe(true);
    expect(thoughts.some((t) => t.id === 'sample-thought-clock')).toBe(true);
  });
});
