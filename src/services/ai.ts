import type { AiMode, Thought, UserProfile } from '../domain/types';
import { aiModeHints, aiModeLabels } from '../domain/labels';

export class AiUnavailableError extends Error {
  constructor(message = 'Asystent AI nie jest skonfigurowany.') {
    super(message);
    this.name = 'AiUnavailableError';
  }
}

export interface AiRequest {
  mode: AiMode;
  thought: Thought;
  extra?: string;
}

export interface AiResponse {
  mode: AiMode;
  text: string;
  model?: string;
}

export function isAiConfigured(profile: UserProfile | undefined): boolean {
  return Boolean(profile?.aiConfig.gatewayUrl.trim());
}

export function consentPreview(thought: Thought, extra?: string): string {
  const body = (thought.content || thought.rawText).trim();
  const clipped = body.length > 1200 ? `${body.slice(0, 1200)}…` : body;
  return [thought.title, clipped, extra?.trim()].filter(Boolean).join('\n\n');
}

export function buildAiPayload(request: AiRequest, profile: UserProfile) {
  return {
    mode: request.mode,
    modeLabel: aiModeLabels[request.mode],
    instruction: aiModeHints[request.mode],
    language: 'pl',
    constraints: {
      defaultContribution: 'minimal',
      oneQuestionUnlessExpansion: request.mode !== 'rozwiniecie',
      markUncertainty: true,
      doNotOverwriteUserText: true,
      noFabricatedCitations: true,
    },
    profile: {
      interests: profile.interests,
      currentFocus: profile.currentFocus,
      notesToSelf: profile.notesToSelf,
    },
    thought: {
      id: request.thought.id,
      title: request.thought.title,
      rawText: request.thought.rawText,
      content: request.thought.content,
      types: request.thought.types,
      status: request.thought.status,
      hypothesis: request.thought.hypothesis,
    },
    extra: request.extra ?? '',
    model: profile.aiConfig.model || undefined,
  };
}

export async function requestAiImpulse(
  request: AiRequest,
  profile: UserProfile,
): Promise<AiResponse> {
  const url = profile.aiConfig.gatewayUrl.trim();
  if (!url) throw new AiUnavailableError();
  if (typeof navigator !== 'undefined' && navigator.onLine === false) {
    throw new AiUnavailableError('Brak połączenia — asystent wymaga sieci.');
  }

  let response: Response;
  try {
    response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(buildAiPayload(request, profile)),
    });
  } catch {
    throw new AiUnavailableError('Nie udało się połączyć z bramką AI.');
  }

  if (!response.ok) {
    throw new AiUnavailableError(`Bramka AI odpowiedziała kodem ${response.status}.`);
  }

  const data: unknown = await response.json().catch(() => null);
  const text = extractText(data);
  if (!text) {
    throw new AiUnavailableError('Bramka AI nie zwróciła tekstu w oczekiwanym formacie.');
  }
  return {
    mode: request.mode,
    text,
    model: typeof data === 'object' && data && 'model' in data ? String((data as { model?: string }).model ?? '') : undefined,
  };
}

function extractText(data: unknown): string {
  if (!data || typeof data !== 'object') return '';
  const record = data as Record<string, unknown>;
  if (typeof record.text === 'string' && record.text.trim()) return record.text.trim();
  if (typeof record.content === 'string' && record.content.trim()) return record.content.trim();
  const choices = record.choices;
  if (Array.isArray(choices) && choices[0] && typeof choices[0] === 'object') {
    const message = (choices[0] as { message?: { content?: string }; text?: string }).message;
    if (message && typeof message.content === 'string') return message.content.trim();
    const text = (choices[0] as { text?: string }).text;
    if (typeof text === 'string') return text.trim();
  }
  return '';
}
