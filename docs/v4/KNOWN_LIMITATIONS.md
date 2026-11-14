# KNOWN_LIMITATIONS — NeuroLab (stan na 2026-11-14)

Zasada: żadna funkcja nie jest opisana jako „gotowa”, jeśli nie została uruchomiona.

## Z MIEJSCA budowania i testowania (szczerze)

1. **Brak emulatora i telefonu w środowisku budującym.** Aplikacja NIE została uruchomiona na urządzeniu/emulatorze.
   Zweryfikowano: kompilację (CI), lint (0 błędów), testy jednostkowe JVM (21), build APK (21,5 MB).
   NIE zweryfikowano: layoutu ekranów, animacji, klawiatury, zachowania IME, grantów uprawnień,
   realnych gestów, ścieżek SAF przy imporcie/eksportu plików, działania aplikacji po rotacji.
2. **Brak klucza dostawcy AI.** Ścieżki z prawdziwym modelem NIE były uruchamiane end-to-end.
   Backend jest pokryty testami kontraktowymi na fake providerze (15/15 PASS lokalnie).
   Zachowanie z prawdziwym dostawcą może się różnić (np. kształt błędów, nagłówki) — mapowanie
   błędów opiera się na kodzie HTTP i strukturze `{error:{message}}` zgodnie z konwencją OpenAI.
3. **HTTP vs HTTPS:** aplikacja dopuszcza cleartext (LAN). Nie testowano połączenia po LAN z telefonu.

## Produktowe / architektoniczne

4. **Bezpieczeństwo at-rest**: myśli, rozmowy i wiadomości leżą jawnie w Room (brak SQLCipher).
   Kto ma telefon — ma dane. To świadomy kompromis lokalności; nie ma synchronizacji multi-device.
5. **Eksport z załącznikami**: zawartość binarna trafia jako base64 do jednego JSON-a — przy wielu
   obrazach plik eksportu może być duży (dziesiątki MB). Nie ma podziału na archiwum.
6. **Wipe kasuje też backupy pre-import** (tabela meta) — po „Wyczyść laboratorium” nie ma
   czego przywracać. Eksportuj wcześniej, jeśli chcesz szansę na powrót.
7. **Backup pre-import** trzyma ostatnią kopię (`keep=1`) — starsze są przycinane.
8. **Kontekst AI** wysyła wybór myśli/projektów/źródeł do dostawcy — to zawsze Twoja decyzja;
   podgląd pokazuje treść JAWNIE, ale odpowiedzialność za wysłanie jest po stronie użytkownika.
9. **Tryb Badanie** bez web-search: model oznacza pewność, ale NIE ma dostępu do internetu —
   „[do sprawdzenia]” to nie jest weryfikacja bibliograficzna.
10. **Backend referencyjny nie ma kont/auth** — jednoosobowa bramka; publikuje się za reverse proxy/VPN.
11. **PWA (apps/web-v2) to zaparkowany prototyp** — nie jest synchronizowany z Androidem, nie jest
    wspierany dalej w V4. Android jest produktem docelowym.
12. **iOS / desktop / sync** — poza zakresem V4.
13. **Lint**: 0 błędów, ~55 warningów (niezwiązane z V4: starsze wzorce Compose, ikony, itp.).
14. **Voice/TTS, OCR, obrazy w modelu, mapy** — P2 V4, niewdrożone; brak przycisków „na niby”.

## Czego NIE wolno twierdzić o tej wersji

- „Aplikacja działa na telefonie” — nie wiemy, dopóki jej nie zainstalujesz i nie klikniesz.
- „AI odpowiada poprawnie” — kontrakt jest przetestowany; realna jakość zależy od dostawcy i modelu.
- „Migracja jest przetestowana” — DDL zweryfikowany skryptem sqlite3 (PASS), NIE na instalacji z danymi produkcyjnymi.
