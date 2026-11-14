import { describe, expect, it } from 'vitest';
import { EXPORT_FORMAT, EXPORT_VERSION, type LabExport } from './types';
import { parseLabExport } from './exportSchema';
import { defaultProfile } from '../db/database';

function sample(): LabExport {
  return {
    format: EXPORT_FORMAT,
    version: EXPORT_VERSION,
    exportedAt: '2024-05-01T00:00:00.000Z',
    thoughts: [],
    projects: [],
    relationships: [],
    sources: [],
    readingItems: [],
    sparks: [],
    profile: defaultProfile(),
  };
}

describe('parseLabExport', () => {
  it('accepts a valid payload', () => {
    const parsed = parseLabExport(sample());
    expect(parsed.format).toBe('neurotrino-lab');
  });

  it('rejects a foreign file', () => {
    expect(() => parseLabExport({ hello: true })).toThrow(/Niepoprawny plik/);
  });

  it('rejects a wrong version', () => {
    expect(() => parseLabExport({ ...sample(), version: 99 })).toThrow(/Niepoprawny plik/);
  });
});
