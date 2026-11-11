import { db } from '../db/database';
import type { AttachmentMeta, AttachmentRecord } from '../domain/types';
import { createId, nowIso } from './ids';

export async function addImageAttachment(thoughtId: string, file: File): Promise<AttachmentMeta> {
  const record: AttachmentRecord = {
    id: createId('att'),
    thoughtId,
    name: file.name,
    mime: file.type || 'application/octet-stream',
    size: file.size,
    createdAt: nowIso(),
    blob: file,
  };
  await db.attachments.put(record);
  const { blob: _blob, ...meta } = record;
  return meta;
}

export async function listAttachmentMeta(thoughtId: string): Promise<AttachmentMeta[]> {
  const rows = await db.attachments.where('thoughtId').equals(thoughtId).toArray();
  return rows.map(({ blob: _blob, ...meta }) => meta);
}

export async function getAttachmentUrl(id: string): Promise<string | undefined> {
  const row = await db.attachments.get(id);
  if (!row) return undefined;
  return URL.createObjectURL(row.blob);
}

export async function deleteAttachment(id: string): Promise<void> {
  await db.attachments.delete(id);
}
