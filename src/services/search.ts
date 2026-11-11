import type { Project, ReadingItem, Thought } from '../domain/types';
import { matchesQuery, thoughtHaystack } from './searchCore';

export { matchesQuery, thoughtHaystack } from './searchCore';

export function searchThoughts(thoughts: Thought[], query: string): Thought[] {
  if (!query.trim()) return thoughts;
  return thoughts.filter((thought) => matchesQuery(thoughtHaystack(thought), query));
}

export function searchProjects(projects: Project[], query: string): Project[] {
  if (!query.trim()) return projects;
  return projects.filter((project) =>
    matchesQuery(`${project.title} ${project.purpose} ${project.description} ${project.nextStep}`, query),
  );
}

export function searchReading(items: ReadingItem[], query: string): ReadingItem[] {
  if (!query.trim()) return items;
  return items.filter((item) =>
    matchesQuery(`${item.title} ${item.author} ${item.notes} ${item.progressNote}`, query),
  );
}

export function recentThoughts(thoughts: Thought[], limit = 8): Thought[] {
  return [...thoughts]
    .filter((t) => t.status !== 'archived')
    .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    .slice(0, limit);
}

export function lastResumableThought(thoughts: Thought[]): Thought | undefined {
  return [...thoughts]
    .filter((t) => t.status !== 'archived')
    .sort((a, b) => b.lastOpenedAt.localeCompare(a.lastOpenedAt))[0];
}
