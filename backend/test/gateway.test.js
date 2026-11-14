"use strict";
const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

// Fake provider (OpenAI-compatible) — ephemeral port, żadnych zewnętrznych wywołań.
let providerServer;
let providerPort;
let providerBehavior = { mode: "ok", delayMs: 0 };

let gateway;
let gwPort;
// JEDEN hook before — node --test nie gwarantuje kolejności między wieloma hookami.
before(async () => {
  providerServer = http.createServer((req, res) => {
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      const send = (status, obj) => {
        res.writeHead(status, { "content-type": "application/json" });
        res.end(JSON.stringify(obj));
      };
      if (providerBehavior.mode === "hang") return; // brak odpowiedzi → timeout klienta
      if (req.url === "/v1/models" || req.url === "/models") {
        if (providerBehavior.mode === "models401") return send(401, { error: "bad key" });
        if (providerBehavior.mode === "models500") return send(500, {});
        return send(200, { object: "list", data: [{ id: "fake-model-a" }, { id: "fake-model-b" }] });
      }
      if (req.url.endsWith("/chat/completions")) {
        if (providerBehavior.mode === "ok") {
          const parsed = JSON.parse(body || "{}");
          return send(200, {
            model: parsed.model || "fake-model-a",
            choices: [{ index: 0, message: { role: "assistant", content: "FAKE: " + (parsed.messages?.at(-1)?.content || "").slice(0, 40) }, finish_reason: "stop" }],
            usage: { prompt_tokens: 10, completion_tokens: 5 },
          });
        }
        if (providerBehavior.mode === "auth401") return send(401, { error: { message: "nope" } });
        if (providerBehavior.mode === "ratelimit429") return send(429, {});
        if (providerBehavior.mode === "server500") return send(500, {});
        if (providerBehavior.mode === "badshape200") return send(200, { choices: [] });
      }
      send(404, {});
    });
  });
  await new Promise((r) => providerServer.listen(0, "127.0.0.1", r));
  providerPort = providerServer.address().port;
  process.env.AI_API_KEY = "test-key";
  process.env.AI_BASE_URL = `http://127.0.0.1:${providerPort}/v1`;
  process.env.AI_MODEL = "fake-model-a";
  process.env.PORT = "0";
  gateway = require("../server.js");
  await new Promise((r) => gateway.listen(0, "127.0.0.1", r));
  gwPort = gateway.address().port;
});
after(() => { gateway?.close(); providerServer?.close(); });

async function call(method, path, body) {
  const res = await fetch(`http://127.0.0.1:${gwPort}${path}`, {
    method,
    headers: body ? { "content-type": "application/json" } : {},
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json = null; try { json = JSON.parse(text); } catch {}
  return { status: res.status, json };
}

test("GET /health zwraca status i provider.configured", async () => {
  providerBehavior.mode = "ok";
  const { status, json } = await call("GET", "/health");
  assert.equal(status, 200);
  assert.equal(json.status, "ok");
  assert.equal(json.provider.configured, true);
  assert.equal(json.provider.reachable, true);
});

test("GET /v1/ai/status raportuje model", async () => {
  providerBehavior.mode = "ok";
  const { status, json } = await call("GET", "/v1/ai/status");
  assert.equal(status, 200);
  assert.equal(json.configured, true);
  assert.equal(json.model, "fake-model-a");
});

test("GET /v1/models proxyfikuje listę", async () => {
  providerBehavior.mode = "ok";
  const { status, json } = await call("GET", "/v1/models");
  assert.equal(status, 200);
  assert.deepEqual(json.models, ["fake-model-a", "fake-model-b"]);
});

test("POST /v1/chat/completions — happy path z kontekstem", async () => {
  providerBehavior.mode = "ok";
  const { status, json } = await call("POST", "/v1/chat/completions", {
    conversationId: "conv-1",
    mode: "impuls",
    messages: [{ role: "user", content: "Czy fotony mają masę?" }],
    context: { currentThought: "Rozważam naturę światła", items: [{ kind: "project", id: "p1", title: "Fizyka", text: "Notatka o fotonach" }] },
  });
  assert.equal(status, 200);
  assert.equal(json.conversationId, "conv-1");
  assert.equal(json.role, "assistant");
  assert.match(json.content, /^FAKE:/);
  assert.equal(json.usage.inputTokens, 10);
  assert.ok(json.messageId);
});

test("tryb adwersarz trafia jako system prompt (przez preview)", async () => {
  providerBehavior.mode = "ok";
  const { status, json } = await call("POST", "/v1/context/preview", {
    mode: "adwersarz",
    messages: [{ role: "user", content: "Mój pomysł: teleportacja przez tunelowanie" }],
    context: { currentThought: "Badam granice fizyki" },
  });
  assert.equal(status, 200);
  assert.match(json.messages[0].content, /ADWERSARZ/);
  assert.match(json.messages[0].content, /KONTEKST/);
});

test("context/preview jest spójny z chat (te same messages)", async () => {
  const body = {
    mode: "analiza",
    messages: [{ role: "user", content: "Przeanalizuj X" }],
    context: { currentThought: "myśl", items: [{ kind: "concept", id: "c1", title: "X", text: "treść X" }] },
  };
  const preview = await call("POST", "/v1/context/preview", body);
  assert.equal(preview.status, 200);
  assert.ok(preview.json.contextChars > 0);
  assert.equal(preview.json.messages.length, 2); // system + user
});

test("Nieznany tryb → 400 INVALID_REQUEST", async () => {
  const { status, json } = await call("POST", "/v1/chat/completions", { mode: "bogus", messages: [{ role: "user", content: "x" }] });
  assert.equal(status, 400);
  assert.equal(json.error.code, "INVALID_REQUEST");
});

test("Pusta lista messages → 400", async () => {
  const { status, json } = await call("POST", "/v1/chat/completions", { messages: [] });
  assert.equal(status, 400);
  assert.equal(json.error.code, "INVALID_REQUEST");
});

test("401 providera → PROVIDER_AUTH_FAILED", async () => {
  providerBehavior.mode = "auth401";
  const { status, json } = await call("POST", "/v1/chat/completions", { messages: [{ role: "user", content: "x" }] });
  assert.equal(status, 502);
  assert.equal(json.error.code, "PROVIDER_AUTH_FAILED");
  assert.equal(json.error.retryable, false);
});

test("429 providera → RATE_LIMITED retryable", async () => {
  providerBehavior.mode = "ratelimit429";
  const { status, json } = await call("POST", "/v1/chat/completions", { messages: [{ role: "user", content: "x" }] });
  assert.equal(status, 429);
  assert.equal(json.error.code, "RATE_LIMITED");
  assert.equal(json.error.retryable, true);
});

test("500 providera → PROVIDER_UNAVAILABLE retryable", async () => {
  providerBehavior.mode = "server500";
  const { status, json } = await call("POST", "/v1/chat/completions", { messages: [{ role: "user", content: "x" }] });
  assert.equal(status, 503);
  assert.equal(json.error.code, "PROVIDER_UNAVAILABLE");
});

test("Odpowiedź 200 o złym kształcie → INTERNAL_ERROR", async () => {
  providerBehavior.mode = "badshape200";
  const { status, json } = await call("POST", "/v1/chat/completions", { messages: [{ role: "user", content: "x" }] });
  assert.equal(status, 502);
  assert.equal(json.error.code, "INTERNAL_ERROR");
});

test("Za duże żądanie (context) → CONTEXT_TOO_LARGE", async () => {
  const huge = "x".repeat(9000);
  const { status, json } = await call("POST", "/v1/chat/completions", { messages: [{ role: "user", content: huge }] });
  assert.equal(status, 413);
  assert.equal(json.error.code, "CONTEXT_TOO_LARGE");
});

test("Niepoprawny JSON → 400 INVALID_REQUEST", async () => {
  const res = await fetch(`http://127.0.0.1:${gwPort}/v1/chat/completions`, { method: "POST", headers: { "content-type": "application/json" }, body: "{nope" });
  const json = await res.json();
  assert.equal(res.status, 400);
  assert.equal(json.error.code, "INVALID_REQUEST");
});

test("Nieznany endpoint → 404", async () => {
  const { status } = await call("GET", "/v1/nope");
  assert.equal(status, 404);
});
