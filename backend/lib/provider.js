"use strict";
// Adapter OpenAI-compatible (chat/completions + models). Zero zależności — globalny fetch (Node ≥18).

class ProviderError extends Error {
  constructor(code, message, retryable) {
    super(message);
    this.name = "ProviderError";
    this.code = code;
    this.retryable = retryable;
  }
}

function mapHttpError(status) {
  if (status === 401 || status === 403) return new ProviderError("PROVIDER_AUTH_FAILED", "Klucz API dostawcy jest nieprawidłowy lub odrzucony.", false);
  if (status === 404) return new ProviderError("MODEL_NOT_FOUND", "Model nie istnieje u dostawcy albo nazwa jest błędna.", false);
  if (status === 429) return new ProviderError("RATE_LIMITED", "Dostawca zgłosił limit żądań. Spróbuj ponownie za chwilę.", true);
  if (status >= 500) return new ProviderError("PROVIDER_UNAVAILABLE", "Usługa dostawcy jest chwilowo niedostępna.", true);
  return new ProviderError("INTERNAL_ERROR", `Dostawca zwrócił nieoczekiwany status ${status}.`, false);
}

async function fetchJson(url, { method = "GET", headers = {}, body, timeoutMs }) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const res = await fetch(url, {
      method,
      headers: { "content-type": "application/json", ...headers },
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
    const text = await res.text();
    let json = null;
    try { json = text ? JSON.parse(text) : null; } catch { /* keep null */ }
    return { status: res.status, json, rawText: text };
  } catch (err) {
    if (err?.name === "AbortError") {
      throw new ProviderError("TIMEOUT", "Dostawca nie odpowiedział w wyznaczonym czasie.", true);
    }
    throw new ProviderError("PROVIDER_UNAVAILABLE", "Nie udało się połączyć z dostawcą modelu.", true);
  } finally {
    clearTimeout(timer);
  }
}

function createProvider({ baseUrl, apiKey, timeoutMs = 20000 }) {
  const base = (baseUrl || "").replace(/\/+$/, "");
  const configured = Boolean(apiKey && base);
  const headers = configured ? { authorization: `Bearer ${apiKey}` } : {};

  async function chatCompletions({ model, messages, temperature, maxOutputTokens }) {
    if (!configured) {
      throw new ProviderError("PROVIDER_UNAVAILABLE", "Backend nie jest skonfigurowany (brak AI_API_KEY albo AI_BASE_URL).", false);
    }
    const res = await fetchJson(`${base}/chat/completions`, {
      method: "POST",
      headers,
      body: { model, messages, temperature, max_tokens: maxOutputTokens },
      timeoutMs,
    });
    if (res.status < 200 || res.status >= 300) throw mapHttpError(res.status);
    const payload = res.json;
    const content = payload?.choices?.[0]?.message?.content;
    if (typeof content !== "string" || !content.trim()) {
      throw new ProviderError("INTERNAL_ERROR", "Dostawca zwrócił pustą lub uszkodzoną odpowiedź.", false);
    }
    const usage = payload?.usage
      ? { inputTokens: payload.usage.prompt_tokens ?? null, outputTokens: payload.usage.completion_tokens ?? null }
      : null;
    return { content, model: payload.model || model, usage };
  }

  async function listModels() {
    if (!configured) {
      throw new ProviderError("PROVIDER_UNAVAILABLE", "Backend nie jest skonfigurowany (brak AI_API_KEY albo AI_BASE_URL).", false);
    }
    const res = await fetchJson(`${base}/models`, { headers, timeoutMs: Math.min(timeoutMs, 8000) });
    if (res.status < 200 || res.status >= 300) throw mapHttpError(res.status);
    const list = Array.isArray(res.json?.data) ? res.json.data : [];
    return list.map((m) => m?.id).filter((id) => typeof id === "string");
  }

  async function ping() {
    if (!configured) return { configured: false, reachable: false, detail: "Brak AI_API_KEY — AI nie jest skonfigurowane." };
    try {
      const res = await fetchJson(`${base}/models`, { headers, timeoutMs: 5000 });
      if (res.status >= 200 && res.status < 300) return { configured: true, reachable: true, detail: "Dostawca odpowiada." };
      if (res.status === 401 || res.status === 403) return { configured: true, reachable: false, detail: "Klucz API odrzucony (401/403)." };
      return { configured: true, reachable: false, detail: `Dostawca odpowiedział statusem ${res.status}.` };
    } catch (err) {
      if (err instanceof ProviderError && err.code === "TIMEOUT") {
        return { configured: true, reachable: false, detail: "Dostawca nie odpowiada (timeout)." };
      }
      return { configured: true, reachable: false, detail: "Brak połączenia z dostawcą." };
    }
  }

  return { chatCompletions, listModels, ping, isConfigured: () => configured };
}

module.exports = { createProvider, ProviderError };
