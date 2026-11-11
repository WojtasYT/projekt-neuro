# Migracja danych z prototypu PWA do Androida

Repozytorium **nie ma dostępu** do IndexedDB w Twojej przeglądarce. Przeniesienie jest ręczne i jawne.

1. W prototypie PWA otwórz **Więcej / Ustawienia → Eksportuj JSON**.
2. Zachowaj plik `neurotrino-lab-….json`.
3. W aplikacji Android: **Ustawienia → Wczytaj zapis**.
4. Domyślnie import **zastępuje** lokalną bazę (wcześniej powstaje kopia w tabeli `meta`).
5. Zdjęcia z PWA **nie wchodzą** do JSON — trzeba je dołączyć ponownie przez wybór zdjęcia.

Walidacja: pole `format` musi być `neurotrino-lab`, `version` = 1. Błędny plik pokaże konkretny komunikat, bez cichego kasowania rekordów.
