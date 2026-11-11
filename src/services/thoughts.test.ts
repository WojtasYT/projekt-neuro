import { describe, expect, it } from 'vitest';
import { splitFragments } from './thoughts';

describe('splitFragments', () => {
  it('splits blank-line separated threads', () => {
    expect(splitFragments('pierwsza\n\ndruga\n\n\ntrzecia')).toEqual([
      'pierwsza',
      'druga',
      'trzecia',
    ]);
  });

  it('keeps a single fragment intact', () => {
    expect(splitFragments('jedna myśl\nz nową linią')).toEqual(['jedna myśl\nz nową linią']);
  });
});
