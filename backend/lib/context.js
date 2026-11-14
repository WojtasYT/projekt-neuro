"use strict";
// Budowanie kontekstu dla modelu — jedyne źródło prawdy dla /v1/chat/completions
// i /v1/context/preview (spójność gwarantowana konstrukcyjnie).

const MAX_CONTEXT_CHARS_DEFAULT = 14000;
const MAX_ITEM_CHARS_DEFAULT = 2000;
const MAX_THOUGHT_CHARS_DEFAULT = 8000;
const MAX_ITEMS_DEFAULT = 20;

const SYSTEM_PROMPTS = {
  impuls: [
    "Jesteś NeuroLab — asystentem analitycznym. Tryb: IMPULS.",
    "Odpowiadaj BARDZO krótko: jedno nieoczywiste skojarzenie, pytanie otwierające albo kontrast.",
    "Maksymalnie 3–4 zdania. Nie rozwiązuj całego problemu. Nie dodawaj rozwinięcia, chyba że użytkownik go zażąda.",
    "Rozdzielaj wiedzę naukową od hipotez. Nie wymyślaj źródeł. Jeśli nie wiesz — powiedz wprost.",
  ].join("\n"),
  rozwiniecie: [
    "Jesteś NeuroLab — asystentem analitycznym. Tryb: ROZWINIĘCIE.",
    "Pomagasz użytkownikowi rozwijać WŁASNĄ myśl: mechanizmy, zależności, pytania uzupełniające.",
    "Zachowaj kolejność i klimat rozumowania użytkownika. Nie przejmuj autorstwa pomysłu.",
    "Kończ pytaniem, które pozwala użytkownikowi zrobić kolejny krok samodzielnie.",
    "Rozdzielaj fakt, konsensus, hipotezę i spekulację. Nie wymyślaj źródeł. Niepewność oznaczaj wprost.",
  ].join("\n"),
  analiza: [
    "Jesteś NeuroLab — asystentem analitycznym. Tryb: ANALIZA.",
    "Rozłóż koncepcję na: założenia, fakty, wnioski, mechanizmy, mocne i słabe strony, alternatywne wyjaśnienia, testowalne przewidywania.",
    "Używaj jawnej struktury. Rozdzielaj to, co jest ugruntowane naukowo, od interpretacji i spekulacji.",
    "Zaproponuj 1–2 falsifyjące obserwacje. Nie wymyślaj źródeł, autorów ani DOI — jeśli źródeł nie znasz, napisz to.",
    "Użytkownik zachowuje autorstwo koncepcji.",
  ].join("\n"),
  badanie: [
    "Jesteś NeuroLab — asystentem analitycznym. Tryb: BADANIE.",
    "Konfrontuj koncepcję użytkownika z aktualną wiedzą naukową.",
    "Jeśli nie masz dostępu do wyszukiwarki, powiedz to wprost i oznacz każdą część odpowiedzi: [znane], [konsensus], [prawdopodobne], [hipoteza], [spekulacja], [do sprawdzenia].",
    "Podaj, co dokładnie należy sprawdzić (publikacje, DOI, instytucje) — ale NIE wymyślaj tytułów, autorów ani identyfikatorów.",
    "Nigdy nie podając fikcyjnych źródeł. Jawne luki są lepsze niż płynne zmyślenie.",
  ].join("\n"),
  adwersarz: [
    "Jesteś NeuroLab — asystentem analitycznym. Tryb: ADWERSARZ.",
    "Krytycznie atakuj rozumowanie użytkownika: szukaj błędów, alternatywnych hipotez, ukrytych założeń, potwierdzenia błędu miedzy pomiarem a wnioskiem.",
    "Nie bądź protekcjonalny. Bądź rzeczowy i konkretny. Ostateczny sąd należy do użytkownika.",
    "Wskaż, co by tę koncepcję osłabiło lub obaliło, oraz co by ją wzmocniło.",
    "Nie wymyślaj źródeł. Niewiedza oznaczona jest lepsza niż zmyślone pokrycie.",
  ].join("\n"),
};

const MODES = Object.keys(SYSTEM_PROMPTS);

function systemPrompt(mode) {
  return SYSTEM_PROMPTS[mode] || null;
}

function normalize(text) {
  return String(text).replace(/\s+/g, " ").trim().toLowerCase();
}

/**
 * Zbuduj blok kontekstu użytkownika + policz prowenancję.
 * Priority: currentThought > items (project/concept/source) — każdy blok z nagłówkiem.
 * Deduplikacja: ten sam id albo znormalizowany tekst → pomijany.
 * Zwraca { block, summary: {includedThoughts, includedProjects, includedSources, includedConcepts, chars} }.
 */
function buildContextBlock({ context, maxChars, maxItemChars, maxThoughtChars, maxItems } = {}) {
  const limits = {
    maxChars: maxChars ?? MAX_CONTEXT_CHARS_DEFAULT,
    maxItemChars: maxItemChars ?? MAX_ITEM_CHARS_DEFAULT,
    maxThoughtChars: maxThoughtChars ?? MAX_THOUGHT_CHARS_DEFAULT,
    maxItems: maxItems ?? MAX_ITEMS_DEFAULT,
  };
  const parts = [];
  const seen = new Set();
  const summary = { includedThoughts: 0, includedProjects: 0, includedSources: 0, includedConcepts: 0, chars: 0 };
  const thought = typeof context?.currentThought === "string" ? context.currentThought.trim() : "";
  const items = Array.isArray(context?.items) ? context.items.slice(0, limits.maxItems) : [];

  if (thought) {
    const t = thought.slice(0, limits.maxThoughtChars);
    parts.push(`[Aktualna myśl użytkownika]\n${t}`);
    seen.add(normalize(t));
    summary.includedThoughts = 1;
    summary.chars += t.length;
  }

  for (const raw of items) {
    if (!raw || typeof raw !== "object") continue;
    const kind = ["project", "concept", "source", "thought"].includes(raw.kind) ? raw.kind : "concept";
    const id = typeof raw.id === "string" ? raw.id.slice(0, 128) : "";
    const title = typeof raw.title === "string" ? raw.title.slice(0, 200) : "";
    const text = typeof raw.text === "string" ? raw.text.trim().slice(0, limits.maxItemChars) : "";
    if (!text && !title) continue;
    const key = id ? `id:${id}` : `text:${normalize(text || title)}`;
    const body = text || title;
    if (seen.has(`text:${normalize(body)}`) || seen.has(key)) continue;
    seen.add(key);
    seen.add(`text:${normalize(body)}`);
    const label = { project: "Projekt", concept: "Koncepcja", source: "Źródło", thought: "Powiązana myśl" }[kind];
    parts.push(`[${label}${id ? ` (${id})` : ""}]${title ? ` ${title}` : ""}\n${body}`);
    summary.chars += body.length;
    if (kind === "project") summary.includedProjects += 1;
    else if (kind === "source") summary.includedSources += 1;
    else if (kind === "thought") summary.includedConcepts += 1;
    else summary.includedConcepts += 1;
  }

  let block = parts.join("\n\n");
  if (block.length > limits.maxChars) {
    block = block.slice(0, limits.maxChars);
    summary.chars = block.length;
    summary.truncated = true;
  }
  return { block, summary, limits };
}

/**
 * Złóż końcową listę wiadomości wysyłaną do providera.
 * Zwraca { messages, contextChars, summary, truncated } — używane przez
 * /v1/chat/completions ORAZ /v1/context/preview (te same dane i ta sama długość).
 */
function buildUpstreamMessages({ mode, messages, context, limits = {} }) {
  const sys = systemPrompt(mode);
  if (!sys) return { error: { code: "INVALID_REQUEST", message: `Nieznany tryb: ${mode}` } };
  const maxChars = limits.maxContextChars ?? MAX_CONTEXT_CHARS_DEFAULT;
  const { block, summary } = buildContextBlock({ context, maxChars, ...limits });
  const system = block ? `${sys}\n\n=== KONTEKST UŻYTKOWNIKA ===\n${block}` : sys;
  const upstream = [{ role: "system", content: system }];
  for (const m of messages) {
    upstream.push({ role: m.role, content: m.content });
  }
  return {
    messages: upstream,
    contextChars: block.length,
    contextSummary: summary,
    systemChars: sys.length,
  };
}

module.exports = { MODES, SYSTEM_PROMPTS, buildContextBlock, buildUpstreamMessages, MAX_CONTEXT_CHARS_DEFAULT };
