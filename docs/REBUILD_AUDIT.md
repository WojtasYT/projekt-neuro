# REBUILD_AUDIT — audyt wejściowy (V3)

Data: 2026-11-12. Audyt czysto czytelniczy, przed zmianami.

## 1. Stan repozytorium

- Katalog: `/home/user/projekt-neuro`, gałąź robocza: `arena/c2476586-projekt-neuro` (status: active).
- HEAD: `b582f5b` „Mark Gradle wrapper script executable.” — rodzic `1306228` (= `origin/main`, „Add files via upload”).
- Gałąź `arena/aa0f430b-projekt-neuro` = `b582f5b` (zdolna do odczytu przez `git ls-remote`). Otwartego PR-u: **PR #1 „Neurotrino Lab — działający rdzeń laboratorium myśli”** (`arena/aa0f430b-projekt-neuro` → `main`, +19213/−16, 100 plików, stan OPEN).
- Ta gałąź sesyjna jest odrostem `b582f5b` — zawiera pełny natywny kod Android z PR #1. Nie tworzymy drugiej gałęzi przebudowy; praca idzie na `arena/c2476586-projekt-neuro`.
- Gałęzi `rebuild/neurotrino-android-v3` nie tworzymy (jest dedykowana gałąź sesyjna).
- Tagi: brak. Zdalne: tylko `origin/main` i `origin/arena/...`.

Struktura: PWA (Vite+React+Dexie) w korzeniu jako „prototyp”, moduł natywny `android/` (~4050 linii Kotlin), dokumentacja w `docs/`, `prompts/`.

## 2. Środowisko robocze (pomiar)

- `java`, `javac`, `gradle`, `adb`, `sdkmanager`: **brak**. SDK Androida: brak. Emulator: brak.
- `apt` niedostępny (deb.debian.org → 000). `node v22.22.3`, `npm`, `python 3.11.2` działają.
- Sieć (pomiar 2026-11-12): dozwolone tylko `github.com`, `codeload.github.com`, `api.github.com`, `registry.npmjs.org`, `pypi.org`, `files.pythonhosted.org`.
  - Zablokowane: `services.gradle.org`, `downloads.gradle.org`, `dl.google.com`, `maven.google.com`, `repo.maven.apache.org`, `repo1.maven.org`, `plugins.gradle.org`, `storage.googleapis.com`, `release-assets.githubusercontent.com`, `objects.githubusercontent.com`, `release-assets.githubusercontent.com` — **nie da się pobrać Gradle, AGP, androidx ani Android SDK**.
- Konsekwencja: kompilacja APK i testy w tym sandboksie są niemożliwe. Weryfikacja lokalna ograniczona do: przeglądu statycznego, parsowania składni (tree-sitter-kotlin z PyPI), testów logiki po stronie JS/Python tam, gdzie to sensowne, oraz testów jednostkowych JVM w Gradle — uruchamianych w CI / Android Studio. Gradle wrapper jar odtwarzamy z oficjalnego repo `gradle/gradle` (tag v8.13.0, přes api.github.com) — patrz sekcja 6.

## 3. Android — inwentaryzacja

- Moduł: `:app`, `namespace`/`applicationId` `lab.neurotrino.app`, minSdk 26, compile/targetSdk 36, JDK 17, AGP 8.13.0, Kotlin 2.2.21, KSP, Compose BOM 2026.09.00, Navigation Compose 2.9.4, Room 2.8.1, DataStore (zadeklarowany, **nieużywany**).
- `gradle-wrapper.properties` → Gradle 8.13; **brak `gradle-wrapper.jar`** (README/workflow liczą na pobranie/wygenerowanie).
- Room `neurotrino-lab.db` v1, `fallbackToDestructiveMigration()`, `exportSchema=false`. Tabele: thoughts, projects, relationships, reading, sources, attachments, profile, meta.
- DAO obserwują Flow; repozytorium mapuje na domain; ViewModel `ShellViewModel` (AndroidViewModel) trzyma stan; Navigation Compose z bottom bar (Laboratorium/Myśli/Połączenia/Projekty/Iskry) + FAB „Zapisz myśl”.
- Dane przykładowe seeded (sampleThoughts/Project/Relationships + startowa półka książek). Provenance `sample`.
- Eksport/Import JSON `neurotrino-lab` v1 z walidacją i kopią zapasową do tabeli `meta` przed importem.
- AI: `AiGateway` — POST na `profile.gatewayUrl`, consent checkbox, wynik tylko jako dopisek (prowenancja AI). Bez kluczy w APK. Brak konfiguracji = uczciwy pusty stan.
- Testy: `SearchTest`, `ExportAndAiTest` (unit), `NavigationSmokeTest` (instrumentalny, prawie nic nie sprawdza — same `runCatching`).
- CI: `.github/workflows/android.yml` — `gradle wrapper` gdy jar brak — **niedziałające na runnerze** (brak gradle na PATH); po dodaniu jar zadziała.

## 4. Wykryte defekty (przed naprawą)

### Architektura / poprawność
1. **Studio — utrata aktualizacji (lost update).** `scheduleSave` robi debounce 450 ms na `current.copy(title=…, content=…)`, a inne akcje (typ, status, projekt, tagi, hipoteza) zapisują `current` sprzed edycji → zapis pól od razu nadpisuje świeży tytuł/treść starą wartością. Potrzebny model „jednego szkicu” + scalanie po stronie repo.
2. **`updateThought` gubi typ sprzed zapisu** — OK dla rawText, ale `title` pusty nadpisywany; zachowanie wymaga świadomego kontraktu „scalanie z ostatnim stanem z DB”.
3. **GraphScreen — trafienie w węzeł losowe**: `nodes.getOrNull((tap.x / size.width) * nodes.size)` ignoruje realną pozycję; klikanie węzłów praktycznie nie działa. Brak etykiet, brak filtra relacji (string `graph_filter` istnieje, UI brak), lista relacji nie pokazuje zakresu Projekt/Czytanie.
4. **Błąd DB nie jest renderowany** — `ShellViewModel.error` nigdy nie trafia do UI; użytkownik widzi wieczne „Otwieram laboratorium…” przy uszkodzonej bazie.
5. **Studio — brak potwierdzenia usunięcia** (`studio_delete_confirm` string nieużywany); jw. projekt/książka/wipe.
6. **ThoughtsScreen — martwe filtry**: `status`/`type` filtrowane w pamięci, ale w UI nie ma żadnych akcji je ustawiających (napisy `thoughts_filter_*` nieużyte).
7. **Załączniki — `PickVisualMedia` + `uri.lastPathSegment`**: grant nietrwały; po restarcie `uri` z photo pickera zwykle nie do otwarcia. Nazwa pliku = „image:…”. Brak miniatur, brak usuwania z pliku, `size=0`.
8. **`wipe()` robi re-seed przykładów** po „Wyczyść laboratorium” — sprzeczne z opisem akcji („wrócić do stanu początkowego” rozumiane jako puste laboratorium; inaczej użytkownik nie może się pozbyć przykładów wprost).
9. **`importJson` backup** rośnie bez ograniczeń w `meta`; import zawsze „replace” (brak ścieżki merge mimo istniejących stringów); `import` nie weryfikuje relacji do innych zbiorów (OK, ale komunikat mylący).
10. **Attachments flow w Studio zbierany przez `LaunchedEffect` na `attachmentsFlow` z ręcznym `mutableStateOf`** — działa, ale konwertowane na lifecycle flow jest uczciwsze; przy okazji: brak usuwania plików z dysku przy usuwaniu załącznika myśli ani przy `wipe()`.
11. **AI consent**: checkbox + „Zostanie wysłane” — OK; ale surowy `error` z sieci nie jest tłumaczony; brak trybu „połączenie” — pomijamy (nie mamy serwera do testów).
12. **`CaptureSheet`**: 5 chipów stanu umysłu w pionowej kolumnie full-width — ergonomicznie złe; ma być pozioma lista przewijana.
13. **`Studio` — „Połącz z inną myślą”**: ściana chipów (12 myśli + 10 typów relacji); potrzebny dialog z wyszukiwarką.
14. **Onboarding/Field nie pokazują `resume`/`error`**; `openResume` w `LauchedEffect` nie reaguje na zmianę `openResume` po pierwszym ustawieniu (mniejsze).
15. **Font**: `FontFamily.Serif/SansSerif/Monospace` — systemowe domysły; wymóg projektu = spójna, świadomie wybrana typografia z pełnym wsparciem PL; brak bundlowanych fontów.
16. **DataStore** w zależnościach, nieużywany — do usunięcia.
17. **Studio — pusty `title` w liście „relTarget”** → wyświetla `t.id` (brzydki fallback `th_<uuid>`); ma być „Bez tytułu”.
18. **CI workflow**: `gradle wrapper` bez gradle na PATH — naprawiamy dołączając jar.
19. **`attachThoughtToProject` nie kasuje powiązania** przy odłączaniu (brak operacji „detach”) — odłączanie myśli od projektu nie istnieje.
20. **`setStatus`/`setHypothesis` itd. każdy czyta i zapisuje cały rekord** — poprawne, ale wymaga scentralizowanego merge zamiast wielu ścieżek.

### UX / wygląd
21. Wyszukiwanie myśli ma filtry tylko „Archiwum”; brak sortowania, brak liczniki, brak sekcji stanów; listy `TextButton`/`InstrumentPanel` wizualnie generyczne.
22. `LabField` — dekoracja ok (promieniowe blaski), ale brak kompozycji „laboratorium → wszechświat → warsztat”; brak hierarchii nagłówka, brak znaczników „instrumentu”, brak pustych stanów w Myślach/Projektach/Bibliotece w spójnym stylu.
23. Brak `minWidth` dla chipów, chipy full-width w arkuszu; `FloatingActionButton` z „+” bez ikony; pasek nawigacji domyślny M3 bez adaptacji; brak `contentDescription` dla części ikon.
24. Kopia: generalnie poprawna PL (widziane: „Wróć do wątku”, „Możliwe powiązanie” nieobecne — OK), ale kilka kalk i niejednoznaczności do poprawy w audycie copy (np. „Pracownia myśli” ok, „Inkubator” żargonowy — zostawić z opisem; „Dopiski” ok).
25. Brak ekranu/sekcji **Źródła** (SourceItem istnieje w modelu/eksportcie, ale nie da się ich dodać/zobaczyć) — luka względem „evidence/source handling”.
26. Wygląd jasny (light) istnieje schemat, ale ekrany używają `Canvas` z gradientami pod ciemną bazę — light będzie sztucznie kontrastowy; wymaga przebudowy komponentu tła z tokenami.

## 5. Co zostaje (nie dotykać bez powodu)

- Kontrakt eksportu `neurotrino-lab` v1 (kompatybilność z PWA i wcześniejszym importerem) — **bez zmian formatu**.
- Zachowana kolejność awarii: Room = źródło prawdy; AI nigdy nie zapisuje bez akceptacji; surowy tekst nietykalny.
- PWA w korzeniu — zostaje jako prototyp referencyjny (nie rozwijamy).
- `applicationId`, `minSdk 26`, `singleTop`, shortcuts, `allowBackup=false`, `dataExtractionRules` — zostają.
- Sample data seed i `removeSamples` — zostają (ale `wipe` przestaje odbudowywać przykłady).

## 6. Wnioski środowiskowe

- Build APK w tym środowisku: **niemożliwy** (brak SDK/Gradle/AGP/google()). Naprawa musi przejść przez: (a) statyczny przegląd, (b) tree-sitter parse wszystkich plików Kotlin, (c) testy jednostkowe w CI/Android Studio po stronie użytkownika, (d) weryfikacja zasobów (XML well-formed, res paths, manifest).
- `gradle-wrapper.jar` dostarczymy z `gradle/gradle` @ `v8.13.0` (43 705 B, `gradle/wrapper/gradle-wrapper.jar`) przez `api.github.com`.
- Fonty: bundle OFL (Inter 400/500/600/700, Source Serif 4 400/500/600/700 + Display 600/700 przy opsz 60, JetBrains Mono 400/500/600) jako **statyczne instancje** wygenerowane fonttoolsem z variable TTF z google/fonts (licencje OFL dołączone).

---

## 7. Postęp — stan po przebudowie UI (2026-11-12)

Zrealizowane w tym etapie: wszystkie ekrany (patrz REBUILD_DECISIONS 17–22), naprawa CI (jar + lint), fonty OFL, Leo Polish copy, eksporcie/backup 1 kopia, wipe bez re-seedu, załączniki end-to-end, import opcja Zastąp/Dołącz + kasowanie plików przy replace, jasny motyw tokenami.

**Czego nadal NIE ma:**
- skompilowany APK i uruchomienie — środowisko sandboksowe nie ma SDK/Gradle/AGP (sieć blokuje google());
- uruchomione testy jednostkowe — lokalnie niemożliwe; w CI (ten workflow);
- przegląd UI na ekranie — brak emulatora;
- instrumentalne testy nawigacji — nie uruchamiane;
- uczciwy stan asystenta AI przy braku serwera — jest pokazany, ale nie testowany z prawdziwym endpointem;
- czytnik źródeł w sieci — celowo brak (bez kluczy/serwera);
- eksport attachmentów do pliku JSON — celowo brak (pliki zostają w pamięci aplikacji; odnotowane w UI).

### CI — pierwszy zielony przebieg (2026-11-12)

- Run `43295209979` → **success** (357 s): `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
- Artefakt `app-debug` = **21 379 113 B** (~20,4 MB), ważny do 2027-02-10.
- Naprawione po drodze: gradlew był 494-bajtowym stubem `java -jar`, ale dołączony jar z `gradle/gradle` nie miał `Main-Class` (wrapper nie startował); wrapper-validation w `setup-gradle` odrzucił nieoficjalnie zbudowany jar → usunięto setup-gradle, cache przez `actions/cache`; compose-bom 2026.09.00/2026.08.00 wymagają Compose 1.12 + compileSdk 37 → BOM **2026.06.01**; `android:windowLayoutInDisplayCutoutMode` = NewApi (API 27 > min 26) → usunięty; `androidx.compose.foundation.clip` nie istnieje → `androidx.compose.ui.draw.clip`; brak importu `Row` w CaptureAndOnboarding; brak mappera `AttachmentMeta.toRow()`.
- Lokalnie nadal bez SDK/Gradle — kompilacja/lint/testy wyłącznie przez CI powyżej. Uruchomienie aplikacji na urządzeniu nieprzeprowadzone.
- Lint: 0 błędów, 55 ostrzeżeń (nieblokujące).

Defekty z sekcji 4 zaktualizowane: 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,25 — naprawione lub ograniczone opisowo; 23 (ergonomia szczegółów), 24 (audyt copy — wykonany częściowo), 26 — zrealizowany przez tokeny LabColors.
