> **AKTUALIZACJA (2026-11-14, po wdrożeniu V4):** ten dokument opisuje audyt sprzed wdrożenia.
> Stan faktyczny po implementacji: patrz `docs/v4/FINAL_QA_REPORT.md` (sekcje 2–9) oraz `docs/v4/TECHNICAL_DEBT.md`
> (D1–D15 ze statusami). Krótko: Room v2 + migracja bez fallbacku, backend kontraktowy (15 testów),
> pełny Asystent w UI, eksport v2 z załącznikami, realny health-check. Emulator: nadal niedostępny.

# AUDIT_REPORT — NeuroLab V4 (2026-11-14)

Audyt wykonany **na gałęzi `arena/c2476586-projekt-neuro`** (PR #2, OPEN → `main`). Repozytorium = źródło prawdy; dokumentacja V2/V3 czytana krytycznie.

## 1. Git i PR
- Bazowa gałąź sesyjna od `b582f5b`; pełna przebudowa UI/infra w 13 commitach, szczyt `8fb22d3`. PR #1 (`arena/aa0f430b-projekt-neuro` → `main`, rdzeń) — **nietknięty**. PR #2 — **nietknięty w tej sesji** (poza commitami na tej samej gałęzi).
- Po obccięciu sandboksa (14.11) lokalny wskaźnik gałęzi wskazywał `b582f5b` przy zachowanym drzewie pracy; remote `origin/arena/c2476586-projekt-neuro` był kompletnym, działającym stanem. Wyrównano `git reset --mixed` do remote — **zero utraty zmian** (0 unikalnych linii w drzewie pracy vs remote; binarne sumy kontrolne zgodne).

## 2. Android (stan rzeczywisty)
- Kotlin 2.2.21, Compose BOM **2026.06.01** (wymaganie compileSdk 36; 2026.08/09 → Compose 1.12 wymaga compileSdk 37 i pada `checkDebugAarMetadata`), AGP 8.13.0, Gradle 8.13 (wrapper uruchamiany przez classpath `GradleWrapperMain`), KSP, Room 2.8.1, Navigation Compose 2.9.4. Brak DataStore (usunięty).
- Moduł `:app`, minSdk 26, target/compileSdk 36. Architektura: pojedynczy moduł; warstwy (ui / data/repo / data/db / domain / data/export / data/ai) w pakietach — zgodnie z „nie twórz modułów dla samego podziału”.
- Pokrycie ekranów: Laboratorium, Szybki zapis (arkusz), Myśli (szukaj/filtry/archiwum), Pracownia (surowy zapis, hipoteza, załączniki, źródła, panel AI), Połączenia (graf pierścieniowy + lista), Iskry, Projekty + szczegół, Biblioteka + książka, Ustawienia, Onboarding. Motyw ciemno-first, fonty OFL bundlowane.
- Dane: Room `neurotrino-lab.db` **v1**, `fallbackToDestructiveMigration()` (!!! niezgodne z V4), `exportSchema=false`. encje: thoughts, projects, relationships, reading, sources, attachments, profile, meta.
- Eksport/import: format `neurotrino-lab` **v1** (myśli/projekty/relacje/źródła/lektura/profil; bez rozmów), kopia zapasowa w `meta` (`backup:pre-import:*`, ostatnia 1), import Zastąp/Dołącz; walidacja w `parseExport`; rawText/createdAt nietykalne; prowenancja zachowana.
- AI: `AiGateway` — POST na user-konfigurowany `gatewayUrl` (kontrakt ad-hoc: `{mode, thought, history?}`→`{text}`), consent checkbox z pełnym podglądem, brak klucza w APK, uczciwy stan „brak konfiguracji”. **Brak: rozmów, historii, trybów Impuls/Rozwinięcie/Analiza/Badanie/Adwersarz, health-checku, /v1/* kontraktu.**
- Testy: `SearchTest`, `ExportAndAiTest` (JVM). CI zielone: run 43295209979 — `testDebugUnitTest lintDebug assembleDebug` ✓; APK `app-debug` ~20,4 MB. **0 błędów lint, 55 ostrzeżeń.**
- Uruchomienie na emulatorze: **nigdy nie przeprowadzone** (brak SDK w sandboksie).

## 3. Backend
- **Nie istnieje.** Zero kodu serwerowego w repo. `SPEC_AI_GATEWAY` z V4 specyfikacji — do zbudowania (patrz IMPLEMENTATION_PLAN Etap 2).

## 4. PWA
- `/` (Vite+React+Dexie) — prototyp referencyjny, zaparkowany; **nie decyduje o architekturze Androida**; bez zmian w tej przebudowie.

## 5. CI/CD
- `.github/workflows/android.yml`: checkout → setup-java 17 → `actions/cache` (~/.gradle) → `gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --console=plain`; on failure: grep `e:`/`Error:` → `::error::` annotations (odczytywalne przez API). `upload-artifact` APK. Wrapper-validation celowo pominięta (jar nie ma w oficjalnej liście checksum).

## 6. Bezpieczeństwo
- Klucze: żadnego w repo ✓; cleartext HTTP **włączony** w manifeście (dla user-konfigurowanych serwerów LAN) — do ograniczenia/opisania (RISK_REGISTER R3). Backup chmurowy off ✓ (`data_extraction_rules`). Załączniki: kopiowane do `filesDir/attachments`, kasowane przy usuwaniu — ✓.

## 7. Wniosek
Fundament (dane, UI, eksport, CI) — solidny do kontynuacji; **brakujące filary V4: rozmowy + prawdziwy backend + kontekst + kontraktowy health-check.** Nic nie przepisujemy od zera; rozszerzamy zgodnie z audytem.
