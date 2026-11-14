# NeuroLab AI Gateway (V4)

Bezpieczny punkt pośredni między aplikacją NeuroLab (Android) a dostawcą modelu (OpenAI-compatible).
**Zero zależności npm** — działa na `node:http` i wbudowanym `fetch` (Node ≥ 18, rekomendowane 22).

## Uruchomienie lokalne

```bash
cd backend
cp .env.example .env   # i wpisz własne wartości (albo eksportuj zmienne)
export AI_API_KEY="sk-..."          # bez tego AI działa w trybie 503 (bez fikcyjnych odpowiedzi)
export AI_BASE_URL="https://api.openai.com/v1"
export AI_MODEL="gpt-4o-mini"
npm start              # http://localhost:8787
```

Testy (fake provider — bez sieci i bez klucza):

```bash
npm test
```

## Zmienne środowiskowe

| Zmienna | Domyślnie | Opis |
|---|---|---|
| `AI_API_KEY` | _(brak)_ | klucz dostawcy; brak ⇒ `/health` raportuje `configured:false`, chat → 503 |
| `AI_BASE_URL` | `https://api.openai.com/v1` | endpoint OpenAI-compatible (także Azure/OpenRouter/vLLM/lokalny) |
| `AI_MODEL` | `gpt-4o-mini` | model domyślny; nadpisywalny polem `model` w żądaniu lub przez `/v1/models` |
| `PORT` | `8787` | port nasłuchu (0.0.0.0) |
| `RATE_LIMIT_PER_MIN` | `30` | limit żądań POST per IP na minutę |
| `REQUEST_TIMEOUT_MS` | `20000` | timeout wywołań do dostawcy |

## Endpointy

- `GET /health` — status usługi + `provider:{configured,reachable,detail}` (realne wywołanie `/models`; nic nie jest zgadywane)
- `GET /v1/ai/status` — jak wyżej + domyślny model
- `GET /v1/models` — proxy listy modeli dostawcy
- `POST /v1/chat/completions` — `{conversationId?, mode, model?, messages[], context?, parameters?}` → `{conversationId, messageId, role:"assistant", content, model, usage}`
- `POST /v1/context/preview` — zwraca DOKŁADNIE te `messages`, które poszłyby do providera (ta sama funkcja budująca kontekst) + `contextChars`, `contextSummary`

Tryby (`mode`): `impuls`, `rozwiniecie`, `analiza`, `badanie`, `adwersarz` — każdy mapuje się na dedykowany polski system prompt (`lib/context.js`).

Błędy: `{error:{code,message,retryable}}`, kody wg AI_GATEWAY_CONTRACT: `INVALID_REQUEST` (400) · `PROVIDER_AUTH_FAILED` (502) · `RATE_LIMITED` (429) · `PROVIDER_UNAVAILABLE` (503) · `MODEL_NOT_FOUND` (404) · `CONTEXT_TOO_LARGE` (413) · `TIMEOUT` (504) · `INTERNAL_ERROR` (500/502).

## Bezpieczeństwo

- Żadnych sekretów w repo/logach/odpowiedziach — logi tylko: metoda, ścieżka, status, czas.
- Body ≤ 256 KB; lista messages ≤ 50; pojedyncza wiadomość ≤ 8000 znaków; kontekst ≤ 14000 znaków.
- Rate-limit per IP (sliding window); `cache-control: no-store`; `x-content-type-options: nosniff`.
- Produkcyjnie: wyłącznie HTTPS (reverse proxy), klucz w env/sekretach hostingu. HTTP tylko do LAN-serwera w domu — świadoma decyzja, nie domyślna ścieżka.
