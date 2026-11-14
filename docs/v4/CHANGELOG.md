# CHANGELOG — NeuroLab

Wpisy zgodne z Keep a Changelog; daty w formacie YYYY-MM-DD.

## [Unreleased] — gałąź arena/c2476586-projekt-neuro (V4, 2026-11-14)

### Dodane
- **Backend AI Gateway** (`backend/`, Node, zero zależności): `GET /health`, `GET /v1/ai/status`, `GET /v1/models`, `POST /v1/chat/completions`, `POST /v1/context/preview`. Tryby: impuls, rozwinięcie, analiza, badanie, adwersarz (dedykowane polskie prompty systemowe). Rate-limit per IP, limity rozmiaru, timeouty, ujednolicone błędy `{error:{code,message,retryable}}`, logi bez treści. Testy: 15 × `node:test` (fake provider). CI: `.github/workflows/backend.yml`.
- **Asystent w Androidzie**: rozmowy (lista/szukaj/rename/archive/delete), trwały zapis w Room, tryb rozmowy (5 trybów, przełączany w każdej chwili), kompozytor, retry/cancel, ekrany błędów z kodem z bramki.
- **Kontekst na żywo**: bieżący tekst edytora (write-through) jako `currentThought`; do 1 projektu / 3 źródeł / 4 powiązanych myśli; deduplikacja; każdy element wykluczalny; **Podgląd** pokazuje DOKŁADNĄ treść żądania (ta sama ścieżka kodu w backendzie).
- **„Sprawdź połączenie” w Ustawieniach**: realne `GET /health` (3 stany + detal dostawcy) + pobieranie listy modeli (`/v1/models`) z wyborem modelu. Koniec statycznego tekstu „Połączono”.
- **Studio → „Otwórz w Asystencie”**: wątek powiązany z bieżącą myślą (najbliższy zapisany kontekst), tytuł z 1. linii, proweniencja AI na odpowiedziach.
- **Room 1→2**: tabele `conversations`+`messages` (FK CASCADE), `MIGRATION_1_2` bez `fallbackToDestructiveMigration`; weryfikacja SQL bez emulatora (`android/scripts/verify_migration_1_2.py` — PASS).
- **Eksport v2** (`format=neurotrino-lab, version=2`): + `conversations`, `messages` (od daty, rola, treść, model, proweniencja). Import akceptuje v1 i v2 (v1 → puste rozmowy); integralność message→conversation walidowana; backup pre-import obejmuje rozmowy.
- **Unit testy**: AssistantDomainTest (kontekst, wykluczenia, normalizeGatewayBase, integrity refs, kontrakt trybów), ExportAndAiTest (v1/v2/integralność/wersja 99).
- Dokumenty: docs/v4/{AUDIT_REPORT, CURRENT_ARCHITECTURE, TECHNICAL_DEBT, IMPLEMENTATION_PLAN, RISK_REGISTER, INTEGRATION_AND_SECURITY, BACKEND_DEPLOYMENT_PL, INSTALLATION_GUIDE_PL, USER_GUIDE_PL, KNOWN_LIMITATIONS, FINAL_QA_REPORT, CHANGELOG}.

### Zmienione
- AiMode (7 trybów: iskra/sokrates/…) → **AssistantMode (5 trybów kontraktowych)**; mapowanie starych wartości eksportu → `impuls` (import tolerancyjny).
- `settings_ai_*` teksty: opis bramki (zamiast „serwera zwracającego text”); cleartext udokumentowany w manifeście.
- Walidacja importu: dodatkowo twarda referencja message→conversation.

### Usunięte
- `fallbackToDestructiveMigration()` — dane użytkownika nie są już kasowane przy migracji.
- Statyczny stan połączenia w Ustawieniach.
- `AiPayload`/`AiGateway.request` (ad-hoc) — zastąpione kontraktem V4.

### Zabezpieczenia
- Klucz API wyłącznie na serwerze bramki; APK go nie zawiera.
- Import: walidacja formatu/wersji/integralności + backup przed importem.
- Logi bramki: wyłącznie metadane.

### Znane ograniczenia
- Emulator/telefon NIE były dostępne w środowisku budującym — UI nie zostało uruchomione; patrz KNOWN_LIMITATIONS.md i FINAL_QA_REPORT.md.
- E2E z realnym dostawcą: nieprzetestowane (brak klucza) — testy kontraktowe na fake providerze.
