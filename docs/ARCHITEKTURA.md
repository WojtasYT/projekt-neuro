# Architektura — Neurotrino Lab

## Źródło prawdy

**Produkt główny:** natywna aplikacja Android w `android/` (Kotlin, Jetpack Compose, Room).

**Prototyp:** PWA w katalogu głównym (Vite + React + Dexie). Zachowana jako wzorzec logiki i formatu JSON. Nie rozwijać jej jako aplikacji docelowej.

## Android

- Pakiet: `lab.neurotrino.app`
- UI: Jetpack Compose + Material 3, własny system wizualny laboratorium
- Nawigacja: Navigation Compose, dolny pasek + FAB „Zapisz myśl”
- Dane: Room `neurotrino-lab.db` v1
- Eksport: `{ "format": "neurotrino-lab", "version": 1, ... }` — ten sam kształt co PWA
- AI: `POST` na URL bramki z profilu; oczekiwane `{ "text": "..." }`
- Kopia zapasowa chmury Androida: wyłączona (`allowBackup=false`)

## Wersje narzędzi (dokumentacja Android, 2026)

- AGP 8.13.0, Gradle 8.13, Kotlin 2.2.21, KSP 2.2.21-2.0.4
- Compose BOM 2026.09.00, compileSdk/targetSdk 36, minSdk 26, JDK 17

## PWA (legacy)

IndexedDB, service worker, Dexie. Uruchomienie: `npm run dev`. Testy: `npm test`.
