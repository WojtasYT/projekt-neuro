# REBUILD_DECISIONS — dziennik decyzji (V3)

Format: decyzja — powód — konsekwencja. Numerowane.

1. **Zostajemy na tej gałęzi** (`arena/c2476586-projekt-neuro`), nie tworzymy `rebuild/...` — konwencja Arena: sesja pracuje na własnej gałęzi; PR #1 z gałęzi macierzystej `arena/aa0f430b-projekt-neuro` zostaje nietknięty. Nowy PR: `arena/c2476586-projekt-neuro` → `arena/aa0f430b-projekt-neuro` (dziedziczy się do `main` po scaleniu PR #1) — alternatywa: → `main`, jeśli tak zechce recenzent.
2. **Nie przepisujemy warstwy danych od zera** — Room schema v1 wystarcza; zmieniamy kontrakty scalania i dodajemy brakujące ścieżki (źródła, detach projektu, usuwanie plików). Unikamy migracji i ryzyka utraty danych.
3. **Studio: jeden szkic + scalanie w repozytorium.** Wszystkie edycje komponują pełny `Thought` z lokalnego stanu (title/content/types/status/tags/project/hypothesis) i zapisują go jedną ścieżką; repo scala `rawText`/`createdAt` nietykalne. Koniec lost-update między debounce a chipami.
4. **Załączniki kopiujemy do `filesDir/attachments/`** przy wyborze (zamiast trzymać URI z photo pickera) — trwałość po restarcie, brak uprawnień do pamięci, miniatury przez `BitmapFactory`. Usunięcie myśli/załącznika/wipe kasuje pliki.
5. **Graf: trafienie po odległości + stałe etykiety + filtr relacji.** Usuwamy losowanie z x; focus = najbliższy węzeł w promieniu. Lista relacji dostaje te same filtry. Żadnych krawędzi bez zapisanej relacji.
6. **Fonty bundlowane, OFL, statyczne instancje** (Inter / Source Serif 4 / JetBrains Mono). Zero pobierania fontów w runtime. Polskie diakrytyki zweryfikowane cmap-checkiem przy pieczeniu.
7. **Jasny motyw przebudowany tokenami** (`LabColors` rozszerzające `ColorScheme`) — bez hardcodowanych ciemnych gradientów w komponentach.
8. **AI zostaje za `AiGateway`**; dodawany uczciwy stan „brak konfiguracji” w Ustawieniach + tłumaczenie błędów; bez test-connection (brak serwera — nie udajemy).
9. **Źródła dostają minimalny, realny obieg** w Pracowni: dodaj źródło (tytuł/autorzy/url/doi), podłącz do myśli (`source_for` relacja + `sourceIds`), widoczne i eksportowane. Bez wyszukiwarki sieciowej (brak kluczy/serwera) — uczciwie oznaczone.
10. **`wipe()` = kasowanie danych + świeży profil, bez odtwarzania przykładów.** „Stan początkowy” = puste laboratorium z domyślnym profilem i startową półką książek tylko po pierwszym uruchomieniu (jak dotąd), nie po każdym wipe.
11. **Kopia przed importem: trzymamy 1 najnowszą** (`meta` klucze `backup:pre-import:*`); reszta kasowana. Import z opcją Zastąp/Dołącz.
12. **DataStore usunięty z zależności** (nieużywany). Material Icons Extended zostają (mapa/projekty/iskry).
13. **CI naprawione przez dołączenie `gradle-wrapper.jar`** z oficjalnego repo Gradle @ v8.13.0 + `./gradlew testDebugUnitTest lintDebug assembleDebug`.
14. **Testy jednostkowe rozszerzone**: round-trip import/export (DTO → domena → Room-row → domena), `splitFragments` wielokrotne puste linie, `SparksCatalog.pick` stabilność, `lastResumable` po archiwizacji, walidator `parseExport` dla uszkodzonych plików. Instrumentalny test nawigacji zostaje; bez emulatora nie uruchamiamy — oznaczone jako niezweryfikowane lokalnie.
15. **Kopia PL** — pełny przegląd; interfejs po polsku; etykiety krótkie i naturalne; nazwy kodu po angielsku.
16. **Bez nowych bibliotek** (poza niczym). Zero telemetryki. Eksport bez zmian v1.

---

## Zapis etapu przebudowy UI (2026-11-12, kontynuacja)

17. **Ekrany nowej wersji są kompletne jako Kotlin** — Laboratorium (LabScreen), Myśli (szukaj/filtruj/archiwum), Pracownia (Studio + części w StudioParts), Połączenia (deterministyczny układ pierścieniowy + lista jako alternatywa dostępnościowa), Iskry (catalog + jedna-iskra), Projekty + szczegół, Biblioteka + książka, Ustawienia (profil, wygląd, asystent, eksport/import, prywatność), Szybki zapis (arkusz) i Onboarding. Motyw: ciemno-pierwszorzędowy, fonty bundlowane, tokeny LabColors.
18. **Graf bez fizyki** — układ pierścieniowy wyliczany trygonometrycznie co klatkę rysowania (projekty/świadki/lektura), stabilny, przewidywalny, tani. Hit-test po odległości z radius 44 dp. Fokus = sąsiedztwo; analogia nigdy nie jest przedstawiana jako mechanizm.
19. **Załączniki: pełny obieg** — `filesDir/attachments`, kasowanie przy usuwaniu myśli/załącznika/wipe/imporcie zastępującym; miniatury dekodowane z próbkowaniem 192 px na Dispatchers.IO.
20. **Studio**: jedno pole tytułu + treść; „pierwszy zapis” jako nienaruszalna, rozwijana warstwa; hipoteza jako opcjonalne sekcje; dopiski oznaczone prowenancją; AI tylko po checkboxie z pełną podglądaną treścią.
21. **Weryfikacja**: brak SDK/Gradle/AGP w sandboksie → źródłem prawdy jest CI na gałęzi sesyjnej (testDebugUnitTest + lintDebug + assembleDebug). Lokalnie: tree-sitter-kotlin (składnia) + własny checker zasobów (365 definicji, 413 odwołań @string/@xml/@font/@drawable rozwiązane) + checker duplikatów modifier + audyt cross-file symboli. Nic nie jest oznaczone jako „zbudowane”, dopóki CI nie przejdzie.
22. **`AttachmentMeta.toRow()` dopisane** po audycie cross-file (brakowało mappera — kompilacja by padła). Grep po `.toRow()/.toDomain()/.toMeta()` i nazwach funkcji z VM/AppNav — reszta rozwiązana.
