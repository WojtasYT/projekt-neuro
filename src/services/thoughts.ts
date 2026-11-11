import { db } from '../db/database';
import type {
  Expansion,
  HypothesisFields,
  Provenance,
  Thought,
  ThoughtStatus,
  ThoughtType,
} from '../domain/types';
import { createId, nowIso, previewTitle } from './ids';

export interface CaptureInput {
  text: string;
  types?: ThoughtType[];
  status?: ThoughtStatus;
  projectId?: string;
  readingItemId?: string;
  provenance?: Provenance;
  isSample?: boolean;
}

export function splitFragments(text: string): string[] {
  return text
    .split(/\n\s*\n/g)
    .map((part) => part.trim())
    .filter(Boolean);
}

export async function captureThoughts(input: CaptureInput): Promise<Thought[]> {
  const chunks = splitFragments(input.text);
  const pieces = chunks.length ? chunks : [input.text.trim()];
  const created: Thought[] = [];
  for (const piece of pieces) {
    if (!piece) continue;
    created.push(await createThought({ ...input, text: piece }));
  }
  return created;
}

export async function createThought(input: CaptureInput): Promise<Thought> {
  const stamp = nowIso();
  const raw = input.text.trim();
  const thought: Thought = {
    id: createId('th'),
    title: previewTitle(raw),
    rawText: raw,
    content: raw,
    types: input.types ?? [],
    status: input.status ?? 'seed',
    tags: [],
    createdAt: stamp,
    updatedAt: stamp,
    lastOpenedAt: stamp,
    provenance: input.provenance ?? 'user',
    projectIds: input.projectId ? [input.projectId] : [],
    sourceIds: [],
    readingItemIds: input.readingItemId ? [input.readingItemId] : [],
    isSample: input.isSample,
    expansions: [],
  };
  await db.thoughts.put(thought);
  return thought;
}

export async function updateThought(
  id: string,
  patch: Partial<
    Pick<
      Thought,
      | 'title'
      | 'content'
      | 'types'
      | 'status'
      | 'tags'
      | 'projectIds'
      | 'sourceIds'
      | 'readingItemIds'
      | 'hypothesis'
    >
  >,
): Promise<Thought | undefined> {
  const existing = await db.thoughts.get(id);
  if (!existing) return undefined;
  const next: Thought = {
    ...existing,
    ...patch,
    rawText: existing.rawText,
    updatedAt: nowIso(),
  };
  if (patch.content !== undefined && !existing.rawText.trim()) {
    next.rawText = patch.content;
  }
  if (!next.title.trim()) next.title = previewTitle(next.content || next.rawText);
  await db.thoughts.put(next);
  return next;
}

export async function touchThought(id: string): Promise<void> {
  const existing = await db.thoughts.get(id);
  if (!existing) return;
  await db.thoughts.update(id, { lastOpenedAt: nowIso() });
}

export async function addExpansion(
  thoughtId: string,
  text: string,
  provenance: Provenance,
): Promise<Thought | undefined> {
  const existing = await db.thoughts.get(thoughtId);
  if (!existing) return undefined;
  const expansion: Expansion = {
    id: createId('ex'),
    text: text.trim(),
    provenance,
    createdAt: nowIso(),
  };
  const next: Thought = {
    ...existing,
    expansions: [...existing.expansions, expansion],
    updatedAt: nowIso(),
  };
  await db.thoughts.put(next);
  return next;
}

export async function setHypothesis(
  thoughtId: string,
  hypothesis: HypothesisFields,
): Promise<void> {
  await updateThought(thoughtId, { hypothesis });
}

export async function deleteThought(id: string): Promise<void> {
  await db.transaction('rw', [db.thoughts, db.relationships, db.attachments], async () => {
    await db.thoughts.delete(id);
    const rels = await db.relationships.where('fromId').equals(id).or('toId').equals(id).toArray();
    await db.relationships.bulkDelete(rels.map((r) => r.id));
    const atts = await db.attachments.where('thoughtId').equals(id).toArray();
    await db.attachments.bulkDelete(atts.map((a) => a.id));
  });
}

export async function archiveThought(id: string): Promise<void> {
  await updateThought(id, { status: 'archived' });
}
