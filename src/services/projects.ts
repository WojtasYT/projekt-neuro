import { db } from '../db/database';
import type { Project, ProjectStatus } from '../domain/types';
import { createId, nowIso } from './ids';

export async function createProject(input: {
  title: string;
  purpose?: string;
  description?: string;
  nextStep?: string;
}): Promise<Project> {
  const stamp = nowIso();
  const project: Project = {
    id: createId('pr'),
    title: input.title.trim() || 'Bez nazwy',
    purpose: input.purpose?.trim() ?? '',
    description: input.description?.trim() ?? '',
    status: 'active',
    nextStep: input.nextStep?.trim() ?? '',
    createdAt: stamp,
    updatedAt: stamp,
    history: [{ at: stamp, note: 'Utworzono projekt.' }],
  };
  await db.projects.put(project);
  return project;
}

export async function updateProject(
  id: string,
  patch: Partial<Pick<Project, 'title' | 'purpose' | 'description' | 'status' | 'nextStep'>>,
  historyNote?: string,
): Promise<Project | undefined> {
  const existing = await db.projects.get(id);
  if (!existing) return undefined;
  const stamp = nowIso();
  const next: Project = {
    ...existing,
    ...patch,
    updatedAt: stamp,
    history: historyNote
      ? [...existing.history, { at: stamp, note: historyNote }]
      : existing.history,
  };
  await db.projects.put(next);
  return next;
}

export async function setProjectStatus(id: string, status: ProjectStatus): Promise<void> {
  await updateProject(id, { status }, `Status: ${status}`);
}

export async function deleteProject(id: string): Promise<void> {
  await db.transaction('rw', [db.projects, db.thoughts, db.relationships], async () => {
    await db.projects.delete(id);
    const thoughts = await db.thoughts.toArray();
    const touched = thoughts.filter((t) => t.projectIds.includes(id));
    for (const thought of touched) {
      await db.thoughts.put({
        ...thought,
        projectIds: thought.projectIds.filter((pid) => pid !== id),
        updatedAt: nowIso(),
      });
    }
    const rels = await db.relationships.where('fromId').equals(id).or('toId').equals(id).toArray();
    await db.relationships.bulkDelete(rels.map((r) => r.id));
  });
}

export async function linkThoughtToProject(thoughtId: string, projectId: string): Promise<void> {
  const thought = await db.thoughts.get(thoughtId);
  if (!thought) return;
  if (thought.projectIds.includes(projectId)) return;
  await db.thoughts.put({
    ...thought,
    projectIds: [...thought.projectIds, projectId],
    updatedAt: nowIso(),
  });
}
