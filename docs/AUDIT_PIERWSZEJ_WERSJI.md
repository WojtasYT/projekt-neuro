# Audyt pierwszej wersji (PWA) — Neurotrino Lab

Data: 2026-11-11  
Gałąź: `arena/aa0f430b-projekt-neuro` @ `77988bd`  
Tryb: wyłącznie odczyt (Prompt 00). Ten plik zapisano *po* inspekcji.

## A. Co istnieje

### Stack (zweryfikowany)

| Warstwa | Fakt |
|---|---|
| Produkt główny w repo | PWA: Vite 5.4 + React 18 + TypeScript |
| Routing | `react-router-dom` v6 (`src/App.tsx`) |
| Stan / dane | Dexie 4 → IndexedDB `neurotrino-lab` |
| UI | jeden arkusz CSS (`src/styles/app.css`), bez biblioteki komponentów |
| PWA | `vite-plugin-pwa`, Workbox `generateSW`, `registerSW` w `src/main.tsx` |
| AI | `fetch` na URL bramki z profilu; brak klucza w kliencie (`src/services/ai.ts`) |
| Testy | Vitest + fake-indexeddb; **14/14** przy `npx vitest run` (2026-11-11) |
| Native Android | **brak** katalogu `android/`, Gradle, Kotlina, Compose |
| Java/SDK w środowisku audytu | `java: command not found`; brak `ANDROID_HOME` |

### Mapa folderów

```
AGENTS.md, README.md, package.json, vite.config.ts, index.html
docs/          — profil, wizja, architektura PWA, bonus rozmowy
public/        — favicon.svg
src/App.tsx    — routing
src/screens/   — Lab, Myśli, Studio, Projekty, Graf, Eksploracja, Biblioteka, Ustawienia
src/components — AppShell, CaptureSheet, Onboarding, ThoughtCard
src/db, domain, services, data, hooks, styles
```

W repozytorium **nie ma** notatek użytkownika — tylko seed przykładowy w kodzie.

### Ekrany

Laboratorium, szybki zapis (sheet), Myśli, Pracownia (`/mysli/:id`), Projekty, Połączenia, Eksploracja, Biblioteka, Ustawienia, onboarding (3 slajdy, pomijalny).

### Przechowywanie

IndexedDB (myśli, projekty, relacje, źródła, lektury, iskry, załączniki-blob, profil, meta). Eksport JSON `neurotrino-lab` v1 **bez blobów**. Offline: cache powłoki + IndexedDB. Brak telemetrii.

## B. Co naprawdę działa

| Obszar | Stan | Dowód |
|---|---|---|
| Zapis myśli + autosave | działa | `thoughts.ts`, `StudioScreen.tsx` |
| Surowy `rawText` vs `content`/`expansions` | działa | `updateThought` nie nadpisuje `rawText` |
| Wyszukiwanie (bez ogonków) | działa | `search.test.ts` |
| Projekty + podpinanie myśli | działa | `ProjectDetailScreen.tsx` |
| Relacje użytkownika | działa | Room-less Dexie `relationships.ts` |
| Graf | częściowy | SVG na okręgu, nie pan/zoom; lista jest |
| Biblioteka 7 książek | działa | `starterLibrary.ts` |
| Eksport/import | działa | `exportImport.test.ts` (załączniki poza plikiem) |
| AI live | niedostępne bez bramki | `AiUnavailableError`, testy |
| Iskry | katalog lokalny, nie AI | `data/sparks.ts` |
| Share/Photo Picker Android | brak | PWA |
| Native APK | brak | — |

Komendy audytu: `npx vitest run` → 14 passed. `java` niedostępne.

## C. Wady języka (przykłady z repo)

Nie wymyślano cytatów. Głos jest na ogół polski, ale część etykiet jest kalką, metaforą bez wyjaśnienia albo angielskim w UI.

| # | Obecny tekst | Plik | Dlaczego kuleje | Propozycja |
|---|---|---|---|---|
| 1 | `Thought Studio` | `ThoughtsScreen.tsx:34` | Angielski w nawigacji | `Pracownia myśli` |
| 2 | `Eksploracja` | `AppShell.tsx:35`, `ExploreScreen.tsx:33` | Abstrakcyjny rzeczownik | `Iskry` albo `Trop do myślenia` |
| 3 | `Jedna iskra, nie feed.` | `ExploreScreen.tsx:34` | „feed” i metafora bez treści | `Jeden trop, nie nieskończona lista.` |
| 4 | `ziarno` (status) | `labels.ts:26` | Kalka *seed* | `szkic` |
| 5 | `inkubacja` | `labels.ts:64` | Żargon | `w inkubatorze` / `odkładam` |
| 6 | `Aktualne ognisko uwagi` | `SettingsScreen.tsx:58` | Kalka *focus* | `Nad czym teraz myślisz` |
| 7 | `Pamięć jest lokalna i edytowalna.` | `SettingsScreen.tsx:33` | „Pamięć” jak o modelu | `To, co tu zapiszesz, zostaje na tym urządzeniu.` |
| 8 | `laboratorium myśli` (podtytuł) | `AppShell.tsx:27` | Ozdobnik, nie akcja | `zapis i powrót do wątku` |
| 9 | `Więcej` | `AppShell.tsx:12` | Ukrywa ustawienia | `Ustawienia` |
| 10 | `wszystkie stany` | `ThoughtsScreen.tsx:58` | Żargon statusów | `dowolny etap` |
| 11 | `Impuls lokalny` | `StudioScreen.tsx` | Brzmi jak produkt AI | `Pytanie na rozruch` |
| 12 | `Głębiej` | `ExploreScreen.tsx` | Nie mówi, co się stanie | `Pokaż mechanizm` |
| 13 | `Inny kąt` | `ExploreScreen.tsx` | Kalka *another angle* | `Inny trop` |
| 14 | `autosave` | `StudioScreen.tsx` | Angielski w statusie | `zapis automatyczny` |
| 15 | `URL bramki` | `SettingsScreen.tsx` | Internals | `Adres serwera asystenta` |
| 16 | `Karta hipotezy (opcjonalna)` | `StudioScreen.tsx` | „Karta” z UI-English | `Pola hipotezy — tylko jeśli chcesz` |
| 17 | `Wejdź` (przy projekcie) | `LabScreen.tsx` | Puste | `Otwórz projekt` |
| 18 | `Czytaj` (przy iskrze) | `LabScreen.tsx` | Nie czyta książki | `Pokaż iskrę` |

## D. Diagnoza wizualna

`src/styles/app.css`: ciemne tło, karta + chip + bottom nav. Hierarchia jest czytelna, ale:

- Laboratorium to **stos kart** (wątek / projekt / iskra / niedawno) — wygląda jak szablon notatnika, nie jak instrument badawczy.
- Brak głębi przestrzennej, siatki laboratoryjnej, wizualizacji danych poza prostym SVG.
- Graf: etykiety 11px, węzły na okręgu, brak gestów; na telefonie słaby.
- Bottom nav 5 pozycji + „Eksploracja/Biblioteka” schowane w Ustawieniach — łatwo zgubić.
- Motyw jasny istnieje, ale to inwersja kolorów, nie osobna kompozycja.

## E. Migracja na native Android

**Cel:** Kotlin + Jetpack Compose, Room, DataStore, bez WebView.

Propozycja:

```
android/app/src/main/java/lab/neurotrino/app/
  data/     Room, repozytoria, eksport JSON v1
  domain/   modele, wyszukiwanie, iskry
  ui/       theme, navigation, ekrany, komponenty
```

Z PWA **przenieść koncepcyjnie:** schemat `neurotrino-lab` v1, katalog iskier, półkę książek, tryby AI, rozdział provenance. **Nie przenosić:** React, CSS, Dexie, service worker.

Migracja danych: użytkownik eksportuje JSON z PWA → w Androidzie **Importuj zapis z prototypu**. Brak dostępu do IndexedDB telefonu/przeglądarki z poziomu repozytorium.

Ryzyka budowy w tym środowisku: brak JDK i SDK; Maven Google poza dozwolonym ruchem sieciowym agenta — `assembleDebug` może być niemożliwe tutaj; projekt i tak ma otwierać się w Android Studio u użytkownika.

Wersje docelowe (dokumentacja Android, 2026): AGP **8.13**, Gradle **8.13**, Kotlin **2.2.21**, Compose BOM **2026.09.00**, compileSdk **36**.

## F. Dane i prywatność

- Gateway URL w IndexedDB — OK, ale użytkownik może wkleić klucz w URL; trzeba przestrzec.
- Zgoda przed wysyłką treści — jest w Studio.
- Załączniki nie wchodzą do eksportu — utrata przy migracji.
- `confirm()` przy imporcie — słaby UX, ryzyko zastąpienia danych.
- Brak szyfrowania w spoczynku (akceptowalne, udokumentować).
- Kopie `backup:pre-import` zostają w IndexedDB bez UI do ich odzyskania.

## G. Checklista akceptacji Androida

- [ ] Projekt Gradle otwiera się w Android Studio, `assembleDebug` daje APK
- [ ] Brak WebView jako UI
- [ ] Zapis myśli jednym gestem, trwałość po restarcie procesu
- [ ] Surowy tekst vs AI osobno
- [ ] Relacja typowana, mapa = lista = ta sama baza
- [ ] Wyszukiwanie, projekty, biblioteka, eksport/import v1 (w tym z PWA)
- [ ] AI wyłączone bez bramki, bez fałszywych odpowiedzi
- [ ] Polski idiomatyczny, stringi w zasobach
- [ ] Wizualny instrument badawczy, nie szablon kart
- [ ] PWA zachowana jako prototyp
- [ ] Dokumentacja bez sprzeczności PWA-vs-Android
