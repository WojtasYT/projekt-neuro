import { afterEach, describe, expect, it } from 'vitest';
import { STARTER_LIBRARY } from '../data/starterLibrary';
import { db, defaultProfile, ensureSeeded } from './database';

describe('ensureSeeded', () => {
  afterEach(async () => {
    await db.delete();
  });

  it('loads starter books and labelled sample thoughts once', async () => {
    await ensureSeeded();
    const books = await db.readingItems.toArray();
    const thoughts = await db.thoughts.toArray();
    const profile = await db.profile.get('local');
    expect(books).toHaveLength(STARTER_LIBRARY.length);
    expect(thoughts.every((t) => t.isSample)).toBe(true);
    expect(profile?.onboarded).toBe(false);
    expect(defaultProfile().aiConfig.gatewayUrl).toBe('');

    await ensureSeeded();
    expect(await db.readingItems.count()).toBe(STARTER_LIBRARY.length);
  });
});
