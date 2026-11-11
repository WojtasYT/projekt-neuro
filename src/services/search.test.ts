import { describe, expect, it } from 'vitest';
import type { Thought } from '../domain/types';
import { lastResumableThought, searchThoughts } from './search';

function thought(partial: Partial<Thought> & { id: string; rawText: string }): Thought {
  const stamp = '2024-02-01T10:00:00.000Z';
  return {
    title: partial.title ?? '',
    content: partial.content ?? partial.rawText,
    types: [],
    status: 'seed',
    tags: [],
    createdAt: stamp,
    updatedAt: stamp,
    lastOpenedAt: stamp,
    provenance: 'user',
    projectIds: [],
    sourceIds: [],
    readingItemIds: [],
    expansions: [],
    ...partial,
  };
}

describe('searchThoughts', () => {
  const items = [
    thought({ id: '1', rawText: 'Zegar neuronalny i nowość', tags: ['czas'] }),
    thought({ id: '2', rawText: 'Opsyny w siatkówce', title: 'Światło' }),
    thought({
      id: '3',
      rawText: 'coś innego',
      hypothesis: { proposal: 'gęstość zdarzeń zmienia tempo' },
    }),
  ];

  it('matches Polish diacritics insensitively', () => {
    const result = searchThoughts(items, 'siatkowce');
    expect(result.map((t) => t.id)).toEqual(['2']);
  });

  it('searches hypothesis fields', () => {
    const result = searchThoughts(items, 'gestosc');
    expect(result.map((t) => t.id)).toEqual(['3']);
  });

  it('returns all when query is empty', () => {
    expect(searchThoughts(items, '  ')).toHaveLength(3);
  });
});

describe('lastResumableThought', () => {
  it('skips archived and prefers last opened', () => {
    const items = [
      thought({
        id: 'old',
        rawText: 'a',
        lastOpenedAt: '2024-01-01T00:00:00.000Z',
        status: 'exploring',
      }),
      thought({
        id: 'new',
        rawText: 'b',
        lastOpenedAt: '2024-05-01T00:00:00.000Z',
        status: 'paused',
      }),
      thought({
        id: 'arch',
        rawText: 'c',
        lastOpenedAt: '2024-06-01T00:00:00.000Z',
        status: 'archived',
      }),
    ];
    expect(lastResumableThought(items)?.id).toBe('new');
  });
});
