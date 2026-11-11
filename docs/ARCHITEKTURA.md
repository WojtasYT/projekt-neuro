# Architektura — Neurotrino Lab

## Decyzje

- **PWA (Vite + React + TypeScript).** Aplikacja ma działać na Androidzie i desktopie, lokalnie, bez konta.
- **IndexedDB (Dexie), schemat v1.** Źródłem prawdy są dane na urządzeniu. Migracje będą kolejnymi wersjami Dexie.
- **Brak backendu w tym repozytorium.** Rdzeń (zapis, studio, projekty, graf, biblioteka, eksport) nie wymaga serwera.
- **AI tylko przez bramkę.** Frontend wysyła JSON na URL podany przez użytkownika. Sekretów nie ma w kodzie, `localStorage` ani IndexedDB.
- **Iskry katalogowe ≠ AI.** Eksploracja korzysta z lokalnego, recenzowanego katalogu i z własnych wątków użytkownika. To jest jawnie oznaczone.
- **Surowy zapis jest niemutowalny.** Edycja idzie w `content` i w `expansions`. `rawText` zostaje.
- **Brak telemetrii.**

## Model

Opisany w `src/domain/types.ts`. Eksport JSON: `{ format: "neurotrino-lab", version: 1, ... }`. Walidacja: Zod (`src/domain/exportSchema.ts`).

## AI — kontrakt bramki

`POST` na URL z profilu, `Content-Type: application/json`.

Ciało zawiera m.in. `mode`, `thought`, `constraints`, `profile` (tylko to, co użytkownik trzyma w profilu).

Oczekiwana odpowiedź:

```json
{ "text": "Jedno pytanie albo impuls." }
```

Akceptowane są też `content` oraz kształt OpenAI `choices[0].message.content`.

## Offline

Service worker (vite-plugin-pwa) cache’uje powłokę. Zapis i odczyt IndexedDB działają bez sieci. AI i przyszłe wyszukiwanie źródeł — nie.

## Uruchomienie

```bash
npm install
npm run dev
npm test
npm run typecheck
npm run lint
npm run build
```
