# RISK_REGISTER — NeuroLab V4 (2026-11-14)

| ID | Ryzyko | Prawdopodobieństwo | Skutek | Mitygacja | Stan |
|---|---|---|---|---|---|
| R1 | Brak emulatora/SDK w sandboksie → UI niezweryfikowane wzrokowo | pewne | średni | CI build+lint+test; analiza statyczna; lista ręcznych QA w KNOWN_LIMITATIONS | AKCEPTOWANE, udokumentowane |
| R2 | Brak klucza dostawcy AI → ścieżka "realny model" nieprzetestowana | wysokie | wysoki | backend poprawny bez klucza (503), testy z fake providerem; konfiguracja = 1 env; docs | AKCEPTOWANE |
| R3 | Cleartext HTTP (serwery LAN użytkownika) | średnie | średni | tylko http dozwolone gdy user wpisze http url; docs ostrzegają; produkcyjnie HTTPS; brak sekretów w app | CZĘŚCIOWO (manifest zostaje, do decyzji usera) |
| R4 | Migracja Room v1→v2 błędna → utrata danych | niskie (hand-written) | wysoki | DDL weryfikowany python-sqlite na DDL v1; bez fallbacku destructive; backup przed importem | AKCEPTOWANE z weryfikacją |
| R5 | Eksport v2 niezgodny ze starszym importem PWA | średnie | niski (PWA zaparkowana) | import 1∪2 po stronie aplikacji; PWA nie konsumuje v2 — udokumentowane | AKCEPTOWANE |
| R6 | 50+ lint warnings narasta | średnie | niski | lint w CI (0 błędów); kolejne iteracje sprzątają | MONITOROWANE |
| R7 | Wrapper jar poza oficjalną listą checksum | wysoka (deterministyczne) | średni | classpath-launch + CI bez setup-gradle validate; komentarz w README | NAPRAWIONE |
| R8 | Koszt tokenów / nadużycie backendu po wystawieniu publicznie | średnie | średni | rate-limit per IP, max payload, timeouty; HTTPS + firewall; docs deploy | MITYGOWANE |
| R9 | Kontekst wysyła zbyt dużo prywatnych danych | niskie | wysoki | preview-only świadomy; chips wykluczeń; `ai_will_send` pokazuje treść; zgody explicit | MITYGOWANE |
| R10 | Wycieki treści w logach backendu | niskie | wysoki | logi tylko metadane (method/path/status/ms); nigdy body | MITYGOWANE |
| R11 | Niespójność /v1/context/preview vs real payload | niskie | średni | ta sama funkcja buildContext dla obu ścieżek; test | NAPRAWIONE (design) |
| R12 | PR #2 do main zmigrowany z błędami → regresja w PR #1 bazie | niskie | wysoki | brak force-push; commity inkrementalne; CI gate | MITYGOWANE |
