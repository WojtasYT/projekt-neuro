export function createId(prefix = 'id'): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}_${crypto.randomUUID()}`;
  }
  return `${prefix}_${Math.random().toString(36).slice(2)}_${Date.now().toString(36)}`;
}

export function nowIso(): string {
  return new Date().toISOString();
}

export function previewTitle(text: string, fallback = 'Bez tytułu'): string {
  const line = text
    .split('\n')
    .map((part) => part.trim())
    .find(Boolean);
  if (!line) return fallback;
  return line.length > 72 ? `${line.slice(0, 69)}…` : line;
}

export function normalizeQuery(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .trim();
}

export function matchesQuery(haystack: string, query: string): boolean {
  if (!query) return true;
  return normalizeQuery(haystack).includes(normalizeQuery(query));
}

export function formatWhen(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('pl-PL', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function formatDay(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('pl-PL', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date);
}
