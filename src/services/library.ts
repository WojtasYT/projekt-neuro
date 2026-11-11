import { db } from '../db/database';
import type { ReadingItem, ReadingStatus, Source } from '../domain/types';
import { createId, nowIso } from './ids';

export async function createReadingItem(input: {
  title: string;
  author?: string;
}): Promise<ReadingItem> {
  const stamp = nowIso();
  const item: ReadingItem = {
    id: createId('bk'),
    title: input.title.trim(),
    author: input.author?.trim() ?? '',
    status: 'unopened',
    progressNote: '',
    currentPage: '',
    notes: '',
    createdAt: stamp,
    updatedAt: stamp,
  };
  await db.readingItems.put(item);
  return item;
}

export async function updateReadingItem(
  id: string,
  patch: Partial<
    Pick<ReadingItem, 'title' | 'author' | 'status' | 'progressNote' | 'currentPage' | 'notes'>
  >,
): Promise<void> {
  const existing = await db.readingItems.get(id);
  if (!existing) return;
  await db.readingItems.put({ ...existing, ...patch, updatedAt: nowIso() });
}

export async function setReadingStatus(id: string, status: ReadingStatus): Promise<void> {
  await updateReadingItem(id, { status });
}

export async function deleteReadingItem(id: string): Promise<void> {
  await db.readingItems.delete(id);
}

export async function createSource(input: Partial<Source> & { title: string }): Promise<Source> {
  const stamp = nowIso();
  const source: Source = {
    id: createId('src'),
    title: input.title.trim(),
    authors: input.authors ?? '',
    year: input.year,
    publisher: input.publisher,
    doi: input.doi,
    url: input.url,
    sourceType: input.sourceType ?? 'other',
    notes: input.notes ?? '',
    accessedAt: input.accessedAt,
    createdAt: stamp,
  };
  await db.sources.put(source);
  return source;
}
