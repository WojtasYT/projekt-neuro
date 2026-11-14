# IMPLEMENTATION_PLAN — NeuroLab V4 (2026-11-14)

Zasada: nie przepisujemy; rozszerzamy. Kolejność wg zależności. Każdy etap kończy się weryfikacją.

## Etap 0 — Naprawa stanu lokalnego (ZROBIONE)
fetch + reset --mixed do origin (sanity binarnych = zgodne). PR #2 nietknięty.

## Etap 1 — Audyt + dokumenty (ZROBIONE w tej sesji)
AUDIT_REPORT / CURRENT_ARCHITECTURE / TECHNICAL_DEBT / RISK_REGISTER / ten plan.

## Etap 2 — Backend AI Gateway (`backend/`, Node ≥20, zero zależności)
Zakres: server.js (node:http, routing), provider.js (adapter OpenAI-compatible: chat/models/ping, timeout przez AbortController, klasyfikacja błędów), context.js (tryby→system prompty PL; budżet znaków; priorytety: aktualna myśl > projekt > koncepcje > źródła; deduplikacja; /v1/context/preview na tej samej logice), ratelimit (in-memory sliding window/IP), walidacja, ustrukturyzowane logi bez sekretów.
Endpointy (AI_GATEWAY_CONTRACT): `GET /health`, `GET /v1/ai/status`, `GET /v1/models`, `POST /v1/chat/completions`, `POST /v1/context/preview`.
Konfiguracja wyłącznie env: `AI_API_KEY`, `AI_BASE_URL`, `AI_MODEL`, `PORT`, limity. **Brak klucza → 503 `PROVIDER_UNAVAILABLE` (configured:false) — zero fikcyjnych odpowiedzi.**
Testy (`node --test`): fake provider (ephemericzny port) — dobra odpowiedź, 401→`PROVIDER_AUTH_FAILED`, 429→`RATE_LIMITED`, timeout→`TIMEOUT`, 5xx→`PROVIDER_UNAVAILABLE`, zły request, zbyt duży kontekst, /health w 3 stanach, /v1/ai/status, /v1/models proxy, spójność /v1/context/preview. **Uruchamialne lokalnie i w CI.**
Zależności: brak (node:*), brak npm install. Deploy: VPS/Docker/Fly/Render — BACKEND_DEPLOYMENT_PL.md.
Kryterium: `node --test` zielony lokalnie + w CI (`.github/workflows/backend.yml`).

## Etap 3 — Android: rozmowy + kontekst (P0)
Room **v1→v2**: encje `ConversationEntity(id, title, projectId?, mode, createdAt, updatedAt, archived)`, `MessageEntity(id, conversationId→FK CASCADE, role, content, model?, provider?, provenance, createdAt)`; DAO+repo; **`MIGRATION_1_2` hand-written; `fallbackToDestructiveMigration` USUNIĘTY.**
Eksport: `format=neurotrino-lab`, `version=2`, dodaje `conversations`+`messages`; `parseExport` akceptuje v1 i v2 (v1 → rozmowy puste). v1 freeze po stronie PWA zachowany (PWA nie czyta v2 — udokumentowane).
Domena (pure, testowalne): `AssistantMode` (impuls/rozwiniecie/analiza/badanie/adwersarz + label/hint), `buildAssistantContext` (budżet 12k znaków, priorytet, deduplikacja, sekcja "Kontekst użytkownika", wykluczenia).
`AiGateway` → kontrakt: `/health`, `/v1/ai/status`, `/v1/models`, `/v1/chat/completions` (conversationId, mode, messages, context{currentThought, projectIds, includedConceptIds, sourceIds}, parameters), mapowanie błędów → PL.
UI `AssistantScreen` (route `assistant`, entry: ikona w Laboratorium + "Otwórz w Asystencie" w Studion): lista rozmów (utwórz/rename/archive/delete/szukaj), czat (tryb chips, input, send/retry/cancel), kontekst: chip "Aktualna myśl" + "Projekt: …" z możliwością wykluczenia; "Zapisz jako koncepcję" → nowa Thought z `Provenance.ai`; historia trwała; błąd czytelny + retry.
Ustawienia: "Sprawdź połączenie" → realne `/health` (+`/v1/ai/status`) z 3 stanami + lista modeli z `/v1/models` (wybór → `profile.model`); koniec statycznego tekstu.
Unit tests: `AssistantContextTest` (budżet/priorytet/dedup/exclude), `ModePromptTest`, mapowanie błędów, tytuł rozmowy z 1. linii.
Kryterium: CI zielone (test+lint+assemble), eksport v2 round-trip w testach, migracja zweryfikowana skryptem SQL (python sqlite3 na DDL v1→v2) — udokumentowana.

## Etap 4 — Eksport/import rozszerzony + spójność (P1)
snapshot/exportJson obejmuje rozmowy; import waliduje id/FK (messages→conversation); „Zapisz odpowiedź jako koncepcję” linkuje projekt rozmowy.

## Etap 5 — Bezpieczeństwo i integracja (P0/P1)
manifest: cleartext TYLKO jawnie opisany; brak sekretów; logi backendu bez treści; rate limit; rozmiar żądań.
Raport integracji: docs/v4/INTEGRATION_AND_SECURITY.md.

## Etap 6 — WYDANIE (częściowe, uczciwe)
FINAL_QA_REPORT.md / KNOWN_LIMITATIONS.md / INSTALLATION_GUIDE_PL.md / BACKEND_DEPLOYMENT_PL.md / USER_GUIDE_PL.md / CHANGELOG.md.
Emulator: niedostępny → wyraźnie oznaczone. APK z CI jako artefakt.

## Zależności
E2 → E3 (kontrakt); E3 → E4/E5; wszystko → E6. E1 blokujący dla pozostałych (spełniony).
## Poza zakresem (świadomie)
OCR/obrazy w modelu, web-search tool use, sync multi-device, iOS, PWA redesign, wyszukiwarka semantyczna, migracja DataStore→Nic (usunięta).
