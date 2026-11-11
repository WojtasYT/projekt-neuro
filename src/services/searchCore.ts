import type { Thought } from '../domain/types';
import { normalizeQuery } from './ids';

export function matchesQuery(haystack: string, query: string): boolean {
  if (!query.trim()) return true;
  return normalizeQuery(haystack).includes(normalizeQuery(query));
}

export function thoughtHaystack(thought: Thought): string {
  const hypo = thought.hypothesis
    ? Object.values(thought.hypothesis).filter(Boolean).join(' ')
    : '';
  const expansions = thought.expansions.map((item) => item.text).join(' ');
  return [
    thought.title,
    thought.rawText,
    thought.content,
    thought.tags.join(' '),
    thought.types.join(' '),
    hypo,
    expansions,
  ].join(' ');
}
