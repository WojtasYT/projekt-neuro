# INTEGRATION_AND_SECURITY — NeuroLab V4 (2026-11-14)

## 1. Topologia

```
[Android (NeuroLab)]  ──HTTPS/HTTP──>  [AI Gateway (backend/)]  ──HTTPS──>  [Dostawca (OpenAI-compatible)]
      Room v2                               node:http, zero deps               OpenAI / Azure / OpenRouter /
      (conversations,                       env: AI_API_KEY, AI_BASE_URL,      vLLM / lokalny model
       messages, myśli)                     AI_MODEL, RATE_LIMIT_PER_MIN
```

- Klucz dostawcy istnieje WYŁĄCZNIE na serwerze bramki (env / sekrety hostingu / VPS `.env`).
- Aplikacja trzyma tylko: adres bramki (`gatewayUrl`), nazwę modelu (`model`). Żadnego sekretu.
- Eksport (`neurotrino-lab` v2) zawiera `aiConfig:{gatewayUrl, model}` — **adres i nazwa modelu, nie klucz**.
  Użytkownik świadomie eksportuje adres własnego serwera; klucz nie wychodzi z serwera.

## 2. Co opuszcza urządzenie (jawność)

Przed każdą wysyłką użytkownik może otworzyć **Podgląd** (Studio: "Zostanie wysłane"; Asystent:
"Podgląd tego, co wyjdzie z telefonu"). Podgląd pokazuje DOKŁADNĄ listę `messages`, które zostaną
wysłane do dostawcy — bo backend liczy je tą samą funkcją, która składa realne żądanie
(`backend/lib/context.js#buildUpstreamMessages`). Android nie ma własnej, równoległej implementacji.

Zawartość kontekstu:
- `currentThought` — aktualny tekst edytowanej myśli (write-through z edytora, nie snapshot);
- do 1 projektu, do 3 źródeł, do 4 powiązanych myśli — z twardymi limitami znaków
  (myśl 8000, element 2000, całość 14000 po stronie serwera);
- deduplikacja po id i znormalizowanym tekście; każdy element można wykluczyć w UI.

## 3. Audyt sekretów

- repo: brak kluczy; `backend/.env.example` zawiera tylko placeholdery; `.env` gitignorowany.
- backend: logi zawierają wyłącznie `method, path, status, ms` — nigdy body, nagłówki, klucze.
- Android: `gatewayUrl`/`model` w `profile` (Room) — akceptowane (nie są sekretami).
- HTTP (cleartext) — TYLKO dla bramki w sieci lokalnej; udokumentowane w manifeście; produkcja = HTTPS.

## 4. Powierzchnie ataku i mitygacje

| Powierzchnia | Mitygacja |
|---|---|
| Body bomb / duże payloady | limit 256 KB; messages ≤ 50; wiadomość ≤ 8000 zn.; kontekst ≤ 14000 zn. |
| Nadużycie publicznego endpointu | rate-limit per IP (sliding window, domyślnie 30/min); `retry-after` |
| Prompt injection z myśli użytkownika | treść użytkownika to dane, nie instrukcje — system prompt wymusza rolę; jawne tryby |
| Provider 401/429/5xx | zmapowane na kody kontraktu (`PROVIDER_AUTH_FAILED`/`RATE_LIMITED`/`PROVIDER_UNAVAILABLE`), brak fikcyjnych odpowiedzi |
| Import złośliwego/uszkodzonego pliku | walidacja formatu/wersji (v1∪v2), integralność message→conversation, PWA-sanity check, backup pre-import w `meta` (retencja 1) |
| Uszkodzony eksport / utrata danych | `exportJson` obejmuje rozmowy; round-trip testowany; wipe nie dotyka backupu w meta? — uwaga: wipe kasuje meta (udokumentowane w KNOWN_LIMITATIONS) |
| Tracking / analityka | brak — aplikacja nie ma telemetrii |

## 5. Co NIE jest chronione (jawnie)

- Treść myśli i rozmów leży jawnie w Room (brak SQLCipher) — typowe dla aplikacji local-first;
  urządzenie niezabezpieczone = brak poufności at-rest. Udokumentowane.
- HTTP w LAN można podsłuchać w tej samej sieci — świadomy wybór użytkownika.
- Backend w wersji referencyjnej nie ma auth użytkowników (jeden posiadacz bramki) —
  jeśli wystawiasz publicznie, postaw go za reverse proxy z auth albo VPN (patrz BACKEND_DEPLOYMENT_PL.md).
