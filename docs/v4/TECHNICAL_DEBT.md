# TECHNICAL_DEBT — NeuroLab V4 (2026-11-14)

| # | Dług | Gdzie | Waga | Plan |
|---|---|---|---|---|
| D1 | `fallbackToDestructiveMigration()` | NeurotrinoDatabase.kt | wysoka | **usunięty** → `addMigrations(MIGRATION_1_2)` |
| D2 | Brak rozmów/historii AI | data/ai, brak tabel | krytyczna (P0) | Etap 2+3: backend + Room v2 |
| D3 | Brak backendu; `AiGateway` = ad-hoc HTTP do profil.gatewayUrl | data/ai | krytyczna (P0) | zbudowanie kontraktu V4 (backend/) |
| D4 | Tryby AI z enuma nieużywane produkcyjnie | domain/Labels | średnia | mapowanie tryb→system prompt backend+Android |
| D5 | Brak health-checku; statyczny „Serwer podpięty" | SettingsScreen | średnia | „Sprawdź połączenie” → realne /health + /v1/ai/status |
| D6 | Eksport v1 bez rozmów | data/export | średnia (P1) | eksport v2 (rozmowy), import 1∪2; wstecznie zgodny |
| D7 | Studio AI panel: osobny, „jednoklikowy” obiekt | StudioScreen | niska | przycisk „Otwórz w Asystencie”; wywołanie przez backend |
| D8 | Cleartext HTTP włączony globalnie | AndroidManifest | średnia | dokumentacja + scoped networkSecurityConfig (dev-only cleartext na prywatny adres? → zostaje allow, bo user LAN; flaga jawna w docs + README) |
| D9 | 55 ostrzeżeń lint | — | niska | przegląd w kolejnych iteracjach; 0 błędów |
| D10 | Graf: etykiety stałe, brak physics — ok, ale brak focus po relacji AI-sugestii | GraphScreen | niska | poza zakresem V4 P0; oznaczenie sugestii istnieje w prowenancji |
| D11 | DataStore unused | build.gradle | naprawiona (usunięty) | — |
| D12 | Brak testów migracji Room | androidTest | średnia | migracja weryfikowana statycznie + przez python-sqlite (DDL v1 vs SQL v2); brak emulatora — udokumentowane |
| D13 | `ExportAndAiTest` zakłada ad-hoc kontrakt | app/src/test | średnia | rozszerzone o assistant-context/mode prompts |
| D14 | Wyjątki `runCatching` w UI (startActivity) | Studio/Settings | niska | świadome, udokumentowane |
| D15 | Brak CI backendu | .github | średnia | backend.yml: node --test |
