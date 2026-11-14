"use strict";
/**
 * NeuroLab AI Gateway (V4) — zero zależności, Node >= 18.
 * Konfiguracja wyłącznie przez env:
 *   AI_API_KEY        — klucz dostawcy (brak => provider niekonfigurowany)
 *   AI_BASE_URL       — endpoint OpenAI-compatible, np. https://api.openai.com/v1
 *   AI_MODEL          — domyślny model (np. gpt-4o-mini); nadpisywalny polem "model" w body
 *   PORT              — domyślnie 8787
 *   RATE_LIMIT_PER_MIN — domyślnie 30
 *   REQUEST_TIMEOUT_MS — domyślnie 20000
 */
const http = require("node:http");
const crypto = require("node:crypto");
const { createProvider, ProviderError } = require("./lib/provider");
const { buildUpstreamMessages, MODES } = require("./lib/context");
const { createRateLimiter } = require("./lib/ratelimit");

const config = {
  port: Number(process.env.PORT || 8787),
  baseUrl: process.env.AI_BASE_URL || "https://api.openai.com/v1",
  apiKey: process.env.AI_API_KEY || "",
  model: process.env.AI_MODEL || "gpt-4o-mini",
  timeoutMs: Number(process.env.REQUEST_TIMEOUT_MS || 20000),
  rateLimitPerMin: Number(process.env.RATE_LIMIT_PER_MIN || 30),
  version: require("./package.json").version,
};

const provider = createProvider({ baseUrl: config.baseUrl, apiKey: config.apiKey, timeoutMs: config.timeoutMs });
const limiter = createRateLimiter({ windowMs: 60_000, max: config.rateLimitPerMin });

const MAX_BODY_BYTES = 256 * 1024;
const MAX_MESSAGES = 50;
const MAX_MESSAGE_CHARS = 8000;
const MAX_TOTAL_CONTENT_CHARS = 24000;
const MAX_CONTEXT_ITEMS = 20;
const MAX_CONTEXT_CHARS = 14000;

function sendJson(res, status, obj) {
  const body = JSON.stringify(obj);
  res.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
    "x-content-type-options": "nosniff",
  });
  res.end(body);
}
function sendError(res, status, code, message, retryable = false) {
  sendJson(res, status, { error: { code, message, retryable } });
}
function getIp(req) {
  const fwd = req.headers["x-forwarded-for"];
  if (typeof fwd === "string" && fwd.trim()) return fwd.split(",")[0].trim().slice(0, 64);
  return req.socket?.remoteAddress || "unknown";
}
function readBody(req) {
  return new Promise((resolve, reject) => {
    let size = 0; const chunks = [];
    req.on("data", (chunk) => {
      size += chunk.length;
      if (size > MAX_BODY_BYTES) {
        reject(Object.assign(new Error("payload too large"), { code: "TOO_LARGE" }));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on("end", () => resolve(Buffer.concat(chunks).toString("utf8")));
    req.on("error", reject);
  });
}
function log(req, res, started) {
  // nigdy: ciała, nagłówki auth, klucze — tylko metadane
  console.log(JSON.stringify({ method: req.method, path: new URL(req.url, "http://x").pathname, status: res.statusCode, ms: Date.now() - started }));
}

function validateChatRequest(raw) {
  const body = raw && typeof raw === "object" ? raw : {};
  const mode = typeof body.mode === "string" ? body.mode : "impuls";
  if (!MODES.includes(mode)) {
    return { error: { code: "INVALID_REQUEST", message: `Nieznany tryb '${mode}'. Dozwolone: ${MODES.join(", ")}.` } };
  }
  const messages = Array.isArray(body.messages) ? body.messages : null;
  if (!messages || messages.length < 1 || messages.length > MAX_MESSAGES) {
    return { error: { code: "INVALID_REQUEST", message: `messages musi być tablicą 1–${MAX_MESSAGES} wpisów.` } };
  }
  let total = 0;
  const cleanMessages = [];
  for (const m of messages) {
    const role = m?.role; const content = m?.content;
    if (!["user", "assistant"].includes(role)) {
      return { error: { code: "INVALID_REQUEST", message: "Każda wiadomość musi mieć role 'user' albo 'assistant'." } };
    }
    if (typeof content !== "string" || !content.trim()) {
      return { error: { code: "INVALID_REQUEST", message: "Wiadomość ma pustą treść." } };
    }
    if (content.length > MAX_MESSAGE_CHARS) {
      return { error: { code: "CONTEXT_TOO_LARGE", message: `Wiadomość przekracza ${MAX_MESSAGE_CHARS} znaków.` } };
    }
    total += content.length;
    cleanMessages.push({ role, content: content.trim() });
  }
  if (total > MAX_TOTAL_CONTENT_CHARS) {
    return { error: { code: "CONTEXT_TOO_LARGE", message: `Łączna długość historii (${total}) przekracza limit ${MAX_TOTAL_CONTENT_CHARS}.` } };
  }
  const context = body.context && typeof body.context === "object" ? body.context : {};
  const items = Array.isArray(context.items) ? context.items.slice(0, MAX_CONTEXT_ITEMS) : [];
  const cleanContext = {
    currentThought: typeof context.currentThought === "string" ? context.currentThought : "",
    items: items.map((i) => ({
      kind: typeof i?.kind === "string" ? i.kind : "concept",
      id: typeof i?.id === "string" ? i.id.slice(0, 128) : "",
      title: typeof i?.title === "string" ? i.title.slice(0, 200) : "",
      text: typeof i?.text === "string" ? i.text : "",
    })),
  };
  const conversationId = typeof body.conversationId === "string" ? body.conversationId.slice(0, 128) : crypto.randomUUID();
  const model = typeof body.model === "string" && body.model.trim() ? body.model.trim().slice(0, 128) : config.model;
  const parameters = body.parameters && typeof body.parameters === "object" ? body.parameters : {};
  const temperature = Number.isFinite(parameters.temperature) ? Math.min(Math.max(parameters.temperature, 0), 2) : 0.4;
  const maxOutputTokens = Number.isFinite(parameters.maxOutputTokens) ? Math.min(Math.max(Math.round(parameters.maxOutputTokens), 1), 4000) : 800;
  return { mode, messages: cleanMessages, context: cleanContext, conversationId, model, temperature, maxOutputTokens };
}

const server = http.createServer(async (req, res) => {
  const started = Date.now();
  res.on("finish", () => log(req, res, started));
  const pathname = new URL(req.url, "http://x").pathname;

  try {
    if (req.method === "GET" && pathname === "/health") {
      const ping = await provider.ping();
      return sendJson(res, 200, {
        status: "ok",
        service: "neurolab-ai-gateway",
        version: config.version,
        provider: ping,
        time: new Date().toISOString(),
      });
    }
    if (req.method === "GET" && pathname === "/v1/ai/status") {
      const ping = await provider.ping();
      return sendJson(res, 200, {
        configured: ping.configured,
        reachable: ping.reachable,
        provider: "openai-compatible",
        model: config.model,
        detail: ping.detail,
      });
    }
    if (req.method === "GET" && pathname === "/v1/models") {
      try {
        const models = await provider.listModels();
        return sendJson(res, 200, { models });
      } catch (err) {
        if (err instanceof ProviderError) {
          const status = { PROVIDER_UNAVAILABLE: 503, PROVIDER_AUTH_FAILED: 502, RATE_LIMITED: 429, TIMEOUT: 504, MODEL_NOT_FOUND: 404 }[err.code] || 502;
          return sendError(res, status, err.code, err.message, err.retryable);
        }
        throw err;
      }
    }
    if (req.method === "POST" && (pathname === "/v1/chat/completions" || pathname === "/v1/context/preview")) {
      const rl = limiter.allow(getIp(req));
      if (!rl.allowed) {
        res.setHeader("retry-after", String(rl.retryAfterSeconds || 30));
        return sendError(res, 429, "RATE_LIMITED", "Zbyt wiele żądań. Spróbuj ponownie za chwilę.", true);
      }
      let raw = "";
      try {
        raw = await readBody(req);
      } catch (err) {
        if (err?.code === "TOO_LARGE") return sendError(res, 413, "CONTEXT_TOO_LARGE", "Żądanie przekracza limit 256 KB.", false);
        throw err;
      }
      let body;
      try { body = raw ? JSON.parse(raw) : {}; } catch {
        return sendError(res, 400, "INVALID_REQUEST", "Body musi być poprawnym JSON-em.");
      }
      const req0 = validateChatRequest(body);
      if (req0.error) {
        const status = req0.error.code === "CONTEXT_TOO_LARGE" ? 413 : 400;
        return sendError(res, status, req0.error.code, req0.error.message, false);
      }

      if (pathname === "/v1/context/preview") {
        const built = buildUpstreamMessages({ mode: req0.mode, messages: req0.messages, context: req0.context, limits: { maxContextChars: MAX_CONTEXT_CHARS } });
        if (built.error) return sendError(res, 400, built.error.code, built.error.message);
        return sendJson(res, 200, {
          mode: req0.mode,
          model: req0.model,
          contextChars: built.contextChars,
          contextSummary: built.contextSummary,
          systemChars: built.systemChars,
          messages: built.messages,
        });
      }

      const built = buildUpstreamMessages({ mode: req0.mode, messages: req0.messages, context: req0.context, limits: { maxContextChars: MAX_CONTEXT_CHARS } });
      if (built.error) return sendError(res, 400, built.error.code, built.error.message);
      try {
        const result = await provider.chatCompletions({
          model: req0.model,
          messages: built.messages,
          temperature: req0.temperature,
          maxOutputTokens: req0.maxOutputTokens,
        });
        return sendJson(res, 200, {
          conversationId: req0.conversationId,
          messageId: crypto.randomUUID(),
          role: "assistant",
          mode: req0.mode,
          content: result.content,
          finishReason: "stop",
          model: result.model,
          usage: result.usage,
        });
      } catch (err) {
        if (err instanceof ProviderError) {
          const status = { PROVIDER_UNAVAILABLE: 503, PROVIDER_AUTH_FAILED: 502, RATE_LIMITED: 429, TIMEOUT: 504, MODEL_NOT_FOUND: 404, CONTEXT_TOO_LARGE: 413 }[err.code] || 502;
          return sendError(res, status, err.code, err.message, err.retryable);
        }
        throw err;
      }
    }
    if (pathname === "/" && req.method === "GET") {
      return sendJson(res, 200, { service: "neurolab-ai-gateway", version: config.version, docs: "GET /health, GET /v1/ai/status, POST /v1/chat/completions, POST /v1/context/preview" });
    }
    return sendError(res, 404, "INVALID_REQUEST", "Nie znaleziono endpointu.");
  } catch (err) {
    console.error(JSON.stringify({ method: req.method, path: pathname, status: 500, ms: Date.now() - started, error: String(err?.message || err) }));
    return sendError(res, 500, "INTERNAL_ERROR", "Błąd wewnętrzny bramki.", true);
  }
});

if (require.main === module) {
  server.listen(config.port, "0.0.0.0", () => {
    console.log(`NeuroLab AI Gateway nasłuchuje na :${config.port} (provider ${config.apiKey ? "skonfigurowany" : "BRAK KLUCZA — AI wyłączone"})`);
  });
}
module.exports = server;
