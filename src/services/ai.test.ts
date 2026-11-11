import { describe, expect, it, vi } from 'vitest';
import { defaultProfile } from '../db/database';
import type { Thought } from '../domain/types';
import { AiUnavailableError, isAiConfigured, requestAiImpulse } from './ai';

const thought: Thought = {
  id: 'th_1',
  title: 'Test',
  rawText: 'surowy',
  content: 'treść',
  types: ['question'],
  status: 'seed',
  tags: [],
  createdAt: '2024-01-01T00:00:00.000Z',
  updatedAt: '2024-01-01T00:00:00.000Z',
  lastOpenedAt: '2024-01-01T00:00:00.000Z',
  provenance: 'user',
  projectIds: [],
  sourceIds: [],
  readingItemIds: [],
  expansions: [],
};

describe('AI boundary', () => {
  it('is not configured without a gateway', () => {
    expect(isAiConfigured(defaultProfile())).toBe(false);
  });

  it('refuses to invent a reply when gateway is missing', async () => {
    await expect(
      requestAiImpulse({ mode: 'iskra', thought }, defaultProfile()),
    ).rejects.toBeInstanceOf(AiUnavailableError);
  });

  it('reads text from a gateway payload', async () => {
    const profile = defaultProfile();
    profile.aiConfig.gatewayUrl = 'https://gateway.example/ai';
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ text: 'Jedno pytanie: co jest założeniem?' }),
      }),
    );
    const result = await requestAiImpulse({ mode: 'sokrates', thought }, profile);
    expect(result.text).toContain('założeniem');
    vi.unstubAllGlobals();
  });
});
