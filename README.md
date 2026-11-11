# Neurotrino Lab

Prywatne laboratorium myśli: zapis, rozwinięcie, połączenia, projekty i powrót do wątku — bez presji produktywności i bez oddawania autorstwa.

To nie jest planer, tracker nawyków ani chatbot.

## Co działa lokalnie

- ekran Laboratorium (szybki zapis, wznowienie, iskra)
- Thought Studio z autosave, stanem, tagami, kartą hipotezy
- surowy oryginalny zapis oddzielony od później dopisywanych warstw
- projekty i przypisywanie myśli
- powiązania między myślami (lista + konstelacja)
- biblioteka z Twoją półką startową
- wyszukiwanie (także bez polskich znaków)
- eksport / import JSON
- PWA / podstawowy tryb offline
- jasny i ciemny motyw

Asystent AI **nie udaje** odpowiedzi, dopóki nie podasz własnej bramki serwerowej.

## Uruchomienie

```bash
npm install
npm run dev
```

Aplikacja nasłuchuje na `0.0.0.0:5173`. Dane zostają w IndexedDB przeglądarki.

## Testy i jakość

```bash
npm test
npm run typecheck
npm run lint
npm run build
```

## Konfiguracja AI (opcjonalna)

1. Postaw własną funkcję serwerową, która trzyma klucz dostawcy.
2. W **Więcej → Asystent AI** wklej URL tej bramki.
3. Przy każdej prośbie aplikacja pokaże, co zostanie wysłane, i wymaga zgody.

Nigdy nie wklejaj klucza API do frontendu.

## Prywatność

- brak konta, analytics i chmury
- treści przykładowe są oznaczone i usuwalne
- „Wyczyść laboratorium” kasuje dane na tym urządzeniu
