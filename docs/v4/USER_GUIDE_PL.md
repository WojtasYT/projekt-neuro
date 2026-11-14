# USER_GUIDE_PL — NeuroLab w 5 minut

NeuroLab to osobiste laboratorium myśli. Łapie to, co Ci umknęłoby za 10 sekund.
Zasada nadrzędna: **Ty myślisz, laboratorium pamięta, AI podpowiada — nigdy nie zastępuje.**

## 1. Zapisywanie myśli

- Przycisk aparatu/„+" na ekranie Laboratorium (albo udostępnij tekst z dowolnej aplikacji → NeuroLab).
- Bez formularzy: wpisz myśl, zapisz, wróć do niej kiedy chcesz. Koniec listy obowiązków.
- Myśl ma: tytuł (pierwsza linia), treść (edytowalna), typy, tagi, status, powiązania.
  **rawText jest nietykalny** — surowa postać Twojej myśli zawsze zostaje zachowana.

## 2. Praca z myślą (Studio)

- **Typy**: obserwacja, pytanie, hipoteza, model, analogia, wynalazek, refleksja…
- **Hipoteza**: pola obserwacja → propozycja → mechanizm → założenia → co wiadomo → falsifiery → następny krok.
- **Powiązania**: wesprzyj się/obalaj/rozszerzaj/analogia/wspólny mechanizm/inspirowane przez…
  AI może *zasugerować* relację — sugestia jest oznaczona i nic nie łączy bez Twojej akceptacji.
- **Proweniencja**: każda treść mówi, skąd pochodzi (Ty / AI / źródło / próbka).

## 3. Asystent (5 trybów)

Wejście: ikona ✦ w Laboratorium, albo Studio → „Otwórz w Asystencie" (bieżąca myśl jako kontekst).

| Tryb | Co robi | Kiedy używać |
|---|---|---|
| **Impuls** | jedno skojarzenie/pytanie, 3–4 zdania | gdy utkniesz i chcesz iskry |
| **Rozwinięcie** | rozwija Twoją myśl, nie przejmuje jej | budowanie wątku |
| **Analiza** | założenia, mechanizmy, falsifiery | porządkowanie koncepcji |
| **Badanie** | konfrontacja z wiedzą naukową (oznacza pewność) | weryfikacja idei |
| **Adwersarz** | krytyka Twojego rozumowania | przed podjęciem decyzji |

Tryb zmienisz w każdej chwili (chipy w rozmowie). Rozmowy zapisują się lokalnie — wrócisz do nich z listy.

**Kontekst**: Asystent widzi aktualną myśl + (opcjonalnie) projekt, źródła i powiązane myśli.
Każdy element możesz wykluczyć chipem. **„Podgląd”** pokazuje DOKŁADNIE, co wyjdzie z telefonu.

Odpowiedź modelu **nie jest Twoją wiedzą** — zostaje w rozmowie jako sugestia. Możesz ją
„Zapisać jako koncepcję” (oznaczy się proweniencją AI) — to Twoja decyzja, nie domyślne zachowanie.

## 4. Graf i projekty

- **Graf** — powiązania między myślami i projektami.
- **Projekty** — inkubatory: cel, opis, następny krok, historia zmian.
- **Biblioteka** — lektury z postępem.

## 5. Dane i bezpieczeństwo

- Wszystko lokalnie (Room). Eksport: Ustawienia → Eksport (JSON, format `neurotrino-lab` v2,
  obejmuje myśli, projekty, relacje, źródła, lektury **i rozmowy z asystentem**).
- Import: waliduje plik, robi kopię zapasową przed importem (zostaje w bazie) — nadpisać dane
  możesz tylko świadomie („Zastąp wszystko”).
- Klucz API do AI mieszka wyłącznie na Twoim serwerze-bramce. Aplikacja go nie widzi.

## 6. Prywatność

Brak telemetrii, brak analityki, brak reklam. Asystent wysyła dokładnie to, co pokaże Ci podgląd.
