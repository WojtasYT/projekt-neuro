import { db } from '../db/database';
import type { GraphNodeKind, Provenance, RelationType, Relationship } from '../domain/types';
import { createId, nowIso } from './ids';

export async function createRelationship(input: {
  fromId: string;
  toId: string;
  fromKind: GraphNodeKind;
  toKind: GraphNodeKind;
  relationType: RelationType;
  explanation?: string;
  provenance?: Provenance;
}): Promise<Relationship | undefined> {
  if (input.fromId === input.toId) return undefined;
  const existing = await db.relationships
    .filter(
      (rel) =>
        rel.fromId === input.fromId &&
        rel.toId === input.toId &&
        rel.relationType === input.relationType,
    )
    .first();
  if (existing) return existing;
  const rel: Relationship = {
    id: createId('rel'),
    fromId: input.fromId,
    toId: input.toId,
    fromKind: input.fromKind,
    toKind: input.toKind,
    relationType: input.relationType,
    explanation: input.explanation?.trim() ?? '',
    provenance: input.provenance ?? 'user',
    createdAt: nowIso(),
  };
  await db.relationships.put(rel);
  return rel;
}

export async function deleteRelationship(id: string): Promise<void> {
  await db.relationships.delete(id);
}

export function neighborsOf(
  id: string,
  rels: Relationship[],
): Relationship[] {
  return rels.filter((rel) => rel.fromId === id || rel.toId === id);
}

export function otherNodeId(rel: Relationship, id: string): string {
  return rel.fromId === id ? rel.toId : rel.fromId;
}
