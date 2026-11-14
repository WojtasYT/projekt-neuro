# FINAL_QA_REPORT — NeuroLab V4 (2026-11-14)

Metoda: dowody z CI i z maszyny budującej (brak emulatora/telefonu/klucza API).
Każda pozycja oznaczona uczciwie: ✅ zrobione i zweryfikowane tutaj · 🟡 zrobione, wymaga potwierdzenia w świecie · ❌ niezrobione.

## 1. Build i jakość statyczna

| Pozycja | Stan | Dowód |
|---|---|---|
| `:app:testDebugUnitTest` | ✅ PASS | CI run 43617123585 (commit 0ea5620) — 21 testów JVM |
| `:app:lintDebug` | ✅ 0 błędów (~55 warningów, przed-V4) | jw. |
| `:app:assembleDebug` | ✅ APK 21 561 939 B (~20,6 MB) | artefakt `app-debug` z CI, exp. 2027-02-12 |
| `node --test backend/` | ✅ 15/15 PASS lokalnie | + CI `backend.yml` run 43617128096 |
| Składnia backendu (`node --check`) | ✅ PASS | CI backend.yml |
| Migracja Room 1→2 (DDL) | ✅ PASS (python+sqlite) | `android/scripts/verify_migration_1_2.py`: wiersze v1 nietknięte, FK CASCADE działa |
| Eksport→import round-trip (v2) | ✅ logika + walidacja testowana jednostkowo | ExportAndAiTest (8 testów) |
| Emulator / telefon | ❌ NIEDOSTĘPNY | ZERO testów UI na urządzeniu |

## 2. P0 — co wymagane i co jest

| Wymóg V4 P0 | Stan | Szczegóły |
|---|---|---|
| Zapis jednoklikowy, brak formularzy | ✅ bez zmian (istniejąca ścieżka capture) | regresji brak (CI zielone) |
| Lokalna baza (Room) | ✅ | v2: + conversations/messages; migracja bezpieczna |
| Projekty | ✅ bez zmian | — |
| Asystent (UI + ścieżka) | ✅ kod + 🟡 nieuruchomiony | rozmowy/rename/archive/delete/search, 5 trybów, retry/cancel |
| Historia czatu trwała | ✅ | Room `messages`, zapis przy każdej wysyłce |
| Backend | ✅ | `backend/` — 5 endpointów kontraktu, zero zależności |
| Konfiguracja | ✅ | env na serwerze; Android trzyma tylko adres+model |
| Bezpieczeństwo | 🟡 | klucz poza APK ✓; cleartext LAN (udokumentowane); import z walidacją + backup; logi bez treści |

## 3. Silnik AI / kontrakt

| Wymóg | Stan | Dowód |
|---|---|---|
| `GET /health` realny, odróżnia down/unconfigured/auth/ratelimit/model | ✅ | `lib/provider.js#ping`; testy 401/429/500; Android `AiGateway.health` + 3 stany w Ustawieniach |
| `POST /v1/chat/completions` | ✅ | fake-provider testy (happy path, 401, 429, 500, bad-shape, 413, 400) |
| `POST /v1/context/preview` = ta sama ścieżka | ✅ | `buildUpstreamMessages` użyta przez oba endpointy; test spójności |
| 5 trybów z różnymi promptami | ✅ | `lib/context.js`; test trybu adwersarz; kontrakt nazw w AssistantDomainTest |
| Kody błędów kontraktu | ✅ | INVALID_REQUEST/PROVIDER_AUTH_FAILED/RATE_LIMITED/PROVIDER_UNAVAILABLE/MODEL_NOT_FOUND/CONTEXT_TOO_LARGE/TIMEOUT/INTERNAL_ERROR |
| Limity/rate-limit | ✅ | 256 KB body, 50 msg, 8000 zn./msg, 14000 zn. kontekst, 30 req/min/IP |
| Klucz nigdy w APK/repo | ✅ | audyt grep: brak; `.env` gitignorowany; placeholder w `.env.example` |
| Realny model E2E | ❌ | brak klucza — „WYMAGA KONFIGURACJI DOSTAWCY — NIEprzetestowane E2E” |

## 4. Kontekst i pamięć

| Wymóg | Stan | Uwagi |
|---|---|---|
| Kontekst z bieżącego tekstu edytora | ✅ | write-through: edytor → Room przy każdej zmianie; Asystent czyta z live flow |
| Budżet/deduplikacja/priorytety | ✅ | 1 projekt / 3 źródła / 4 powiązania; dedup po id i znormalizowanym tekście; limity znaków |
| Podgląd tego, co wyjdzie | ✅ | przycisk „Podgląd” → dialog z `messages` i `contextChars` z backendu |
| Wykluczenia z kontekstu | ✅ | chipy (aktualna myśl + elementy) |
| Proweniencja user/AI | ✅ | `messages.provenance`, `thoughts.provenance`; AI-odpowiedzi oznaczone, nigdy domyślnie nie trafiają do wiedzy |
| Rozmowy nie brudzą wiedzy | ✅ | „Zapisz jako koncepcję” = jawna akcja, proweniencja AI |
| Status poznawczy edytowalny przez usera, nie auto-werdykt AI | ✅ bez zmian | istniejące pole `status` + chipy typów/statusów w Studio |

## 5. Eksport/import

| Wymóg | Stan | Dowód |
|---|---|---|
| Eksport obejmuje rozmowy/wiadomości | ✅ | `format=neurotrino-lab, version=2` |
| Eksport obejmuje załączniki | ✅ | base64 w `AttachmentDto`; brak pliku = `omittedReason` (jawne pominięcie) |
| Eksport obejmuje relacje/daty | ✅ | bez zmian + rozmowy |
| Import waliduje + backup first | ✅ | `parseExport` (format/wersja/integralność message→conversation i attachment→thought); backup do `meta` przed importem; przycinanie do 1 |
| Import nie nadpisuje cicho | ✅ | wybór scalanie/zastąp w UI; „Zastąp” kasuje wyłącznie po potwierdzeniu |
| Import v1 działa | ✅ | test jednostkowy (brak pól conversations/messages → puste) |

## 6. Bezpieczeństwo (skrót)

✅ brak sekretów · ✅ logi bramki bez treści · ✅ rate-limit · ✅ no-store + nosniff · 🟡 cleartext LAN (świadomy, udokumentowany) · 🟡 brak SQLCipher (dokumentacja) · ✅ walidacja importu · ✅ backup pre-import.

Pełna tabela: `docs/v4/INTEGRATION_AND_SECURITY.md`.

## 7. Czego NIE zrobiono (jawnie)

- ❌ **Weryfikacja na emulatorze/telefonie** — wymóg PROMPT_06 niewypełniony; sandbox bez SDK; zero testów UI.
- ❌ **E2E z realnym dostawcą** — brak klucza.
- ❌ **Image analysis / OCR / mapy / auto-sugestie** — P2, poza zakresem; zero atrap.
- ❌ **Sync multi-device, iOS, web-parity PWA** — poza zakresem.
- ❌ **SQLCipher** — jawnie oznaczona luka w KNOWN_LIMITATIONS.
- ❌ **Test migracji na instalacji z danymi produkcyjnymi** — skrypt sqlite zweryfikował DDL i FK; realna instalacja v1→v2 nie była wykonana.

## 8. Werdykt

**NeuroLab V4 dostarcza P0 w postaci kodu, testów i CI-zielonych buildów.** Funkcje asystenta/bramki
nie były uruchomione na urządzeniu ani wobec żywego dostawcy. APK z CI (`app-debug`, 21,5 MB)
jest gotowy do ręcznej instalacji i weryfikacji na telefonie — kluczowe kroki: Ustawienia → Adres bramki AI →
„Sprawdź połączenie” → Asystent → wysłanie wiadomości → podgląd kontekstu.

Ocena uczciwa: **gotowe do wewnętrznych testów użytkownika, NIE do publicznego wydania, dopóki nie zostaną
zaliczone scenariusze P0 na realnym urządzeniu (INSTALLATION_GUIDE_PL.md §6).**

## 9. PR #2 / branch / artifact

- Gałąź `arena/c2476586-projekt-neuro` (PR #2 → main, nietknięty), 8 commitów V4.
- Artefakt CI: `app-debug` (21 561 939 B, exp. 2027-02-12), run 43617123585.
- PR #1 nietknięty. Zero zmian poza `backend/`, `android/`, `.github/workflows/`, `docs/`.
