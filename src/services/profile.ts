import { db, defaultProfile } from '../db/database';
import type { MindState, UserProfile } from '../domain/types';
import { nowIso } from './ids';

export async function getProfile(): Promise<UserProfile> {
  const existing = await db.profile.get('local');
  if (existing) return existing;
  const created = defaultProfile();
  await db.profile.put(created);
  return created;
}

export async function updateProfile(patch: Partial<Omit<UserProfile, 'id'>>): Promise<UserProfile> {
  const current = await getProfile();
  const next: UserProfile = { ...current, ...patch, id: 'local', updatedAt: nowIso() };
  await db.profile.put(next);
  return next;
}

export async function setMindState(state?: MindState): Promise<void> {
  await updateProfile({ lastMindState: state });
}

export function applyTheme(theme: 'dark' | 'light'): void {
  document.documentElement.dataset.theme = theme;
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) meta.setAttribute('content', theme === 'light' ? '#f4efe4' : '#0b1016');
}
