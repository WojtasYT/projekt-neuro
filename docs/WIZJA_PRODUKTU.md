# Wizja produktu — Neurotrino Lab

## 1. Jednozdaniowa definicja

Prywatne, lokalne i mobilne laboratorium myśli, które pomaga użytkownikowi zauważać zjawiska, eksplorować naukę, łączyć odległe koncepcje, rozwijać własne hipotezy i wynalazki oraz wracać do przerwanych wątków — bez przejmowania autorstwa przez AI i bez presji produktywności.

## 2. Pozycjonowanie

To nie jest:
- planer zadań;
- tracker nawyków;
- aplikacja do nauki w szkolnym stylu;
- automatyczny generator teorii;
- niekończący się feed ciekawostek;
- zwykły chatbot z historią rozmów.

To jest połączenie:
- osobistego notatnika badawczego;
- inkubatora hipotez i wynalazków;
- mapy wiedzy i powiązań;
- personalnego eksploratora nauki;
- asystenta pomagającego wznowić myślenie;
- przestrzeni projektowej dla długoterminowych koncepcji.

## 3. Główna pętla produktu

1. **Bodziec:** użytkownik zauważa coś, otrzymuje interesujący fakt albo wraca do starej myśli.
2. **Zapis:** rejestruje obserwację jednym działaniem.
3. **Rozwinięcie:** użytkownik sam dopisuje skojarzenie lub pytanie; AI może dać mały impuls.
4. **Połączenie:** myśl zostaje powiązana z inną koncepcją, źródłem albo projektem.
5. **Weryfikacja:** użytkownik może sprawdzić, co jest znane, co jest spekulacją i co wymaga dowodów.
6. **Powrót:** aplikacja zachowuje stan i proponuje łatwy punkt kontynuacji.

Ta pętla ma działać bez potrzeby ukończenia całego procesu. Samo zapisanie wartościowej obserwacji jest sukcesem.

## 4. Proponowana nawigacja

Nie trzeba pokazywać wszystkich sekcji na ekranie głównym. Proponowana struktura:

- **Laboratorium** — ekran startowy, szybki zapis, wznowienie wątku, pojedyncze iskry.
- **Myśli** — Thought Studio, wyszukiwanie, filtry i archiwum.
- **Połączenia** — interaktywna mapa koncepcji i alternatywny widok relacji.
- **Projekty** — Neurotrino, wynalazki, koncepcje długoterminowe.
- **Eksploracja** — fakty, problemy otwarte, mechanizmy i połączenia między dziedzinami.
- **Biblioteka** — książki, notatki z lektur, źródła i publikacje.
- **Profil / ustawienia** — preferencje, pamięć, prywatność, eksport, AI i wygląd.

Na telefonie nawigacja powinna być prosta i wygodna kciukiem. Główna akcja „Zapisz myśl” powinna pozostać łatwo dostępna.

## 5. Moduły i ich logika

### A. Laboratorium — ekran startowy

Widok powinien dostosować się do kontekstu bez długiego formularza. Priorytetem są trzy akcje:
- Zapisz myśl.
- Wróć do ostatniego wątku.
- Zaskocz mnie.

Poniżej może pojawić się jedna karta aktualnego projektu albo iskra dopasowana do zapisanych myśli.

### B. Myśli — główny notatnik

Każda myśl ma:
- tytuł lub automatycznie skrócony podgląd;
- oryginalny tekst użytkownika;
- osobne rozwinięcia / wersje;
- typ i status;
- tagi;
- powiązane myśli, projekty i źródła;
- daty utworzenia i modyfikacji;
- opcjonalną sekcję hipotezy;
- oznaczenie autorstwa treści (użytkownik / AI / źródło / wspólna edycja).

Dane można edytować, usuwać, archiwizować, wyszukiwać i eksportować. Nie wymuszaj tytułu, typu ani tagów przy szybkim zapisie.

### C. Pracownia hipotez

Elastyczny formularz / płótno, nie sztywny kreator. Pola pojawiają się na życzenie:
- obserwacja;
- pytanie;
- hipoteza;
- proponowany mechanizm;
- założenia;
- znane fakty;
- alternatywne wyjaśnienia;
- przewidywania;
- możliwe falsyfikatory;
- źródła;
- następny bezpieczny krok.

Użytkownik może zacząć od dowolnego pola, pominąć pola i wrócić później.

### D. Silnik połączeń

Pokazuje propozycje relacji między zapisanymi myślami i obszarami wiedzy. Każda propozycja ma krótkie uzasadnienie oraz klasę relacji: mechanizm, analogia, metafora, hipoteza związku, inspiracja lub słaba zbieżność. Sugestie AI wymagają akceptacji przed zapisaniem.

### E. Mapa koncepcji

Węzły: myśli, hipotezy, projekty, pojęcia, źródła i książki.

Relacje: rozwija, wspiera, kwestionuje, jest analogią do, ma wspólny mechanizm z, pochodzi ze źródła, należy do projektu, zainspirowało.

Mapa powinna umożliwiać filtrowanie i skupienie się na sąsiedztwie jednego węzła. Na telefonie dostępny jest widok listy relacji.

### F. Eksploracja

Zamiast feedu nieskończonego przewijania: ograniczona liczba jakościowych propozycji. Kategoria iskry może wynikać z:
- ostatniej myśli;
- dawnego, nieaktywnego wątku;
- aktualnie wybranego projektu;
- losowej obserwacji ze świata;
- konkretnej dziedziny;
- otwartego problemu naukowego.

Każda propozycja powinna dawać możliwość zapisania, pominięcia, zmiany perspektywy lub sprawdzenia źródeł.

### G. Projekty

Projekt jest kontenerem na myśli, źródła, obrazy, notatki, pytania i aktualny kierunek. Może mieć najbliższy mały krok, ale bez obowiązkowego terminarza.

### H. Biblioteka

Książki oraz artykuły są zasobami, nie obowiązkiem. Użytkownik może zapisać stronę/rozdział, własną myśl, fragment dostarczony przez siebie i powiązania z innymi tematami. Nie należy wymuszać codziennej liczby stron.

### I. Asystent AI

Tryby:
- Iskra — minimum interwencji.
- Sokrates — jedno pytanie.
- Łącznik — propozycja powiązania.
- Adwokat diabła — mocna alternatywa i krytyka.
- Weryfikator — sprawdzanie faktów i źródeł.
- Głębsza analiza — tylko na żądanie.
- Wznowienie — przywrócenie kontekstu myśli.

Wszystkie tryby muszą respektować profil użytkownika i zasady autorstwa z `AGENTS.md`.

## 6. Przepływy ograniczające próg rozpoczęcia

### Przepływ: „Nie mogę zacząć”

- Otwórz aplikację.
- Pokaż jedno pole: „Co ostatnio chodzi Ci po głowie?”
- Alternatywa: „Daj mi jedną małą iskrę”.
- Pozwól zakończyć po jednym zdaniu.
- Nie wyświetlaj zaległych zadań ani utraconych streaków.

### Przepływ: „Mam zbyt dużo myśli”

- Zapisz kilka surowych fragmentów bez kategoryzowania.
- Pozwól później łączyć je w wątki.
- Nie wymagaj wyboru najlepszego pomysłu od razu.

### Przepływ: „Boję się, że to już znane”

- Zapisz pierwotną myśl i zachowaj datę jej powstania.
- Opcjonalnie uruchom analizę podobnych koncepcji.
- Wynik przedstaw neutralnie: podobne / znane / pokrewne / nie znaleziono w ograniczonym przeglądzie.
- Wskaż różnice i otwarte pytania.
- Nie oceniaj wartości samego procesu samodzielnego dojścia do pomysłu.

### Przepływ: „Wracam po dłuższej przerwie”

- Pokaż krótki zapis ostatniego kontekstu.
- Podaj jeden możliwy punkt kontynuacji.
- Nie mów o zaległościach i nie wyliczaj dni nieaktywności.

## 7. Model danych — propozycja

Model może zostać dostosowany do istniejącego kodu, ale powinien zachować rozdzielenie bytów:

- `Thought`: id, title, rawText, content, type, status, tags, createdAt, updatedAt, provenance, projectIds, sourceIds.
- `Project`: id, title, purpose, description, status, nextStep, createdAt, updatedAt.
- `Concept`: id, label, domain, description, sourceType.
- `Relationship`: id, fromId, toId, relationType, explanation, provenance, confidence, createdAt.
- `Source`: id, title, authors, year, publishedAt, publisher, doi, url, sourceType, notes, accessedAt.
- `ReadingItem`: id, title, author, status, progressNote, currentPage, linkedThoughtIds.
- `Session`: id, startedAt, endedAt, contextSnapshot, thoughtIds, optional user reflection. Do not track invasive behavioural telemetry.
- `Spark`: id, title, explanation, sourceIds, domainTags, linkedThoughtIds, status, generatedBy.
- `UserProfile`: editable interests, interaction preferences, current focus, opt-in settings.
- `Attachment`: id, file metadata, local blob reference, linked entity ids.

IDs should be stable. Timestamps should be consistent. Use migrations. Preserve raw captures. Avoid storing redundant denormalised blobs unless required for performance.

## 8. Przechowywanie, prywatność i offline

Docelowo local-first:
- IndexedDB for notes, relationships, projects, reading items, preferences, and attachment metadata/blobs.
- Service worker caches app shell and static assets.
- Core read/write flows work offline.
- AI, source search, and current research clearly indicate that they require a connection.
- Export/import JSON with version field and schema validation.
- Optional attachment export if practical; communicate limitations.
- No analytics by default.
- No cloud sync by default.
- No API secret in the browser bundle or repository.

## 9. MVP — kolejność

### MVP 1: prawdziwy rdzeń
1. Responsywny ekran Laboratorium.
2. Szybki zapis myśli.
3. Thought Studio: lista, edycja, usuwanie/archiwizacja, status, wyszukiwanie.
4. Powiązanie dwóch myśli.
5. Proste projekty i przypisywanie myśli do projektu.
6. Local persistence i odtwarzanie po odświeżeniu.
7. Biblioteka książek z listą startową.
8. Import/eksport.

### MVP 2: środowisko eksploracji
1. Mapa koncepcji z edycją powiązań.
2. Iskry i wznowienia wątków.
3. Karta hipotezy z polami opcjonalnymi.
4. Zapisywanie źródeł i powiązań.
5. Tryby AI przez provider-agnostic adapter, jeśli skonfigurowano backend.

### MVP 3: rozszerzona inteligencja
1. Spersonalizowany silnik połączeń.
2. Research workflow i ocena statusu dowodów.
3. Ostrożny przegląd podobnych koncepcji.
4. Odczytywanie / analizowanie przesłanych notatek i zdjęć, jeśli infrastruktura pozwala.
5. Zaawansowane filtrowanie grafu.

Nie budować MVP 3 kosztem stabilności MVP 1.

## 10. Kryteria akceptacji

- Użytkownik potrafi zapisać myśl bez zakładania konta i bez wypełniania metadanych.
- Zapis przetrwa odświeżenie i ponowne uruchomienie.
- Można odnaleźć myśl przez wyszukiwanie.
- Można wznowić ostatni wątek bez presji.
- Można utworzyć projekt i powiązać z nim myśl.
- Można utworzyć relację między dwiema myślami i ją później zmienić/usunąć.
- Można wyeksportować dane i zaimportować poprawny plik.
- Rdzeń działa offline.
- AI nie udaje połączenia, gdy brak konfiguracji.
- Użytkownik wie, kiedy jego treść zostanie przesłana do usługi zewnętrznej.
- Sugestie AI są oddzielone od oryginalnych notatek.
- Interfejs jest wygodny na typowym ekranie Androida.
- Nie ma martwych przycisków ani nieopisanych makiet udających działające funkcje.

## 11. Pomysły na później — nie blokują MVP

- Automatyczne proponowanie powiązań między dawnymi i nowymi notatkami.
- „Kapsuła Kreatywności” jako sesja eksperymentalna.
- Tryb „Obiekt pod mikroskopem” — bezpieczne poznawcze rozkładanie zwykłego obiektu na mechanizmy.
- Tryb „Co jeśli?” z rozróżnieniem praw fizycznych, założeń i fantazji.
- Wizualne osie czasu rozwoju hipotezy.
- Wykrywanie sprzeczności między notatkami bez automatycznego rozstrzygania ich.
- Przegląd dawnych pomysłów, które mogą zyskać nowe znaczenie.
- Personalny tygodniowy przegląd nowej wiedzy — wyłącznie opt-in.
- Integracja z zewnętrznymi źródłami publikacji, jeśli dostępne są wiarygodne API.

## 12. Co oznacza sukces

Sukcesem nie jest „użytkownik otworzył aplikację 30 dni z rzędu”. Sukces to m.in.:
- spontaniczna myśl została zachowana zamiast zniknąć;
- użytkownik wrócił do porzuconego pomysłu bez poczucia winy;
- powstało wartościowe połączenie między dwiema wcześniejszymi koncepcjami;
- użytkownik odróżnił analogię od mechanizmu;
- użytkownik lepiej zrozumiał rzeczywiste zjawisko;
- hipoteza została doprecyzowana albo uczciwie odrzucona;
- pojawiła się nowa droga do dalszego samodzielnego myślenia.
