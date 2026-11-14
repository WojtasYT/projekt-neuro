# TECHNICAL_DEBT — NeuroLab V4 (2026-11-14)

| # | Dług | Gdzie | Waga | Plan |
|---|---|---|---|---|
| D1 | `fallbackToDestructiveMigration()` | NeurotrinoDatabase.kt | wysoka | **usunięty** → `addMigrations(MIGRATION_1_2)` |
| D2 | Brak rozmów/historii AI | data/ai, brak tabel | krytyczna (P0) | **NAPRAWIONE** (Etap 2+3): Room v2 `conversations`+`messages`, pełny UI Asystenta |
| D3 | Brak backendu; `AiGateway` = ad-hoc HTTP do profil.gatewayUrl | data/ai | krytyczna (P0) | **NAPRAWIONE**: `backend/` (zero deps) + kontraktowy klient Android + `/v1/context/preview` |
| D4 | Tryby AI z enuma nieużywane produkcyjnie | domain/Labels | średnia | **NAPRAWIONE**: `AssistantMode` (5 trybów) → prompty systemowe w backendzie (testowane); stary AiMode usunięty |
| D5 | Brak health-checku; statyczny „Serwer podpięty" | SettingsScreen | średnia | **NAPRAWIONE**: „Sprawdź połączenie” = realne GET /health (stan + detal) + /v1/models (wybór modelu) |
| D6 | Eksport v1 bez rozmów | data/export | średnia (P1) | **NAPRAWIONE**: eksport v2 = + conversations/messages/attachments(base64); import 1∪2 z walidacją integralności; round-trip testowany |
| D7 | Studio AI panel: osobny, „jednoklikowy” obiekt | StudioScreen | niska | **NAPRAWIONE**: „Otwórz w Asystencie” (wątek per myśl, kontekst = bieżący tekst z edytora) |
| D8 | Cleartext HTTP włączony globalnie | AndroidManifest | średnia | **JAWNIE ZAAKCEPTOWANE**: tylko bramka usera w LAN; HTTPS w produkcji; komentarz w manifeście + docs/v4/INTEGRATION_AND_SECURITY.md |
| D9 | 55 ostrzeżeń lint | — | niska | przegląd w kolejnych iteracjach; 0 błędów |
| D10 | Graf: etykiety stałe, brak physics — ok, ale brak focus po relacji AI-sugestii | GraphScreen | niska | poza zakresem V4 P0; oznaczenie sugestii istnieje w prowenancji |
| D11 | DataStore unused | build.gradle | naprawiona (usunięty) | — |
| D12 | Brak testów migracji Room | androidTest | średnia | **CZĘŚCIOWO**: DDL zweryfikowany `android/scripts/verify_migration_1_2.py` (PASS: wiersze v1 nietknięte, FK CASCADE działa); testy na emulatorze NIEMOŻLIWE tutaj — REQUIRES_DEVICE |
| D13 | `ExportAndAiTest` zakłada ad-hoc kontrakt | app/src/test | średnia | **NAPRAWIONE**: przepisane na v1∪v2 + integralność + AssistantDomainTest (8 nowych testów) |
| D14 | Wyjątki `runCatching` w UI (startActivity) | Studio/Settings | niska | świadome, udokumentowane |
| D15 | Brak CI backendu | .github | średnia | **NAPRAWIONE**: `.github/workflows/backend.yml` — syntax check + 15 testów na każdy push/PR |
