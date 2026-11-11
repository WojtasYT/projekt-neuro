import type { Spark } from '../domain/types';

/**
 * Curated local sparks. These are not live AI output.
 * Factual claims stay modest and distinguish mechanism, analogy, and open questions.
 */
export const CATALOG_SPARKS: Spark[] = [
  {
    id: 'spark-oddball-time',
    title: 'Nowość i poczucie czasu',
    hook: 'Nowy odcinek drogi potrafi „rozciągnąć” godzinę, a powrót tą samą trasą ją skraca.',
    explanation:
      'Układ nerwowy silniej koduje zdarzenia odbiegające od przewidywania (efekt nowości / oddball). Gęstszy zapis zdarzeń w pamięci sprawia, że wstecznie odcinek wydaje się dłuższy. To opis zjawiska psychologicznego — nie jest to jeszcze fizyczny „zegar” w mózgu.',
    deeper:
      'Dopamina i układy uwagi modulują saliency nowości, ale subiektywny czas ma kilka warstw: czasowe okno percepcji, tempo zdarzeń, pamięć epizodyczna i nastrój. Hipoteza, że „liczba zdarzeń neuronalnych” nadaje tempo subiektywnego czasu, jest Twoją propozycją do sprawdzenia — nie konsensusem.',
    question: 'Gdybyś miał odróżnić tempo zdarzeń od nowości zdarzeń, które z nich bardziej zmienia Twoje poczucie godziny?',
    domains: ['neurobiologia', 'percepcja', 'pamięć'],
    sourceNote:
      'Zjawisko nowości i zniekształcenia retrospektywnego czasu jest opisywane w psychologii poznawczej; konkretny „zegar neuronalny zdarzeń” pozostaje hipotezą.',
    kind: 'observation',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-photon-opsin',
    title: 'Foton, który staje się sygnałem',
    hook: 'Światło nie „wchodzi do umysłu”. Kończy się na zmianie kształtu białka.',
    explanation:
      'Foton o odpowiedniej energii może zostać pochłonięty przez retinal związany z opsyną. Zmiana konformacji białka uruchamia kaskadę w fotoreceptorze, a dopiero później impulsy w siatkówce i korze. Widzenie zaczyna się jako chemia, nie jako obraz.',
    deeper:
      'Różne opsyny mają inne maksima absorpcji — stąd barwa jako rozróżnienie długości fali, nie jako właściwość samego fotonu „na zewnątrz”. Między fotonem a doświadczeniem barwy jest kilka warstw transdukcji.',
    question: 'W którym miejscu tej kaskady kończy się fizyka światła, a zaczyna Twoja interpretacja „widzę”? ',
    domains: ['percepcja', 'neurobiologia', 'fizyka'],
    sourceNote: 'Transdukcja fotoelektryczna w opsynach to dobrze opisany mechanizm biologiczny.',
    kind: 'mechanism',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-seed-travel',
    title: 'Nasiono, które nie „wie” dokąd leci',
    hook: 'Trawa nie planuje podróży. Korzysta z kształtów, które fizyka nagradza.',
    explanation:
      'Nasiona przemieszczają się przez wiatr, zwierzęta, wodę albo wystrzał mechaniczny. Kształt, masa, powierzchnia i kleistość to nie ozdoby — to rozwiązania transportu. Kiełkowanie uruchamia się, gdy wilgoć, temperatura i czasem światło przekroczą próg, a nie gdy nasiono „zdecyduje”.',
    deeper:
      'To dobry przykład, że funkcja może wyglądać na intencję, choć mechanizm jest ślepy. Analogia do innych systemów (w tym nerwowych) jest tu tylko analogią, dopóki nie wskażesz wspólnego procesu.',
    question: 'Jaki zwykły przedmiot obok Ciebie ma kształt, który zdradza, do jakiego środowiska został dopasowany?',
    domains: ['biologia', 'ewolucja', 'fizyka'],
    kind: 'observation',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-myelin-insulation',
    title: 'Mielina: izolacja czy coś więcej?',
    hook: 'Owijka wokół aksonu przyspiesza sygnał, ale porównanie do kabla elektrycznego ma granicę.',
    explanation:
      'Osłonka mielinowa zwiększa opór poprzeczny i zmniejsza pojemność błony, umożliwiając przewodzenie skokowe. To wspólny mechanizm z izolacją kabla tylko częściowo: w nerwie „izolacja” jest żywa, naprawiana i metabolicznie kosztowna.',
    deeper:
      'Uszkodzenie mieliny zmienia nie tylko prędkość, ale timing złożeń synaptycznych. Jeśli dwie sieci mają działać razem, liczy się nie sam fakt połączenia, lecz chwila nadejścia.',
    question: 'Gdzie w Twoich pomysłach „przewodzenie” jest metaforą, a gdzie naprawdę chodzi o timing?',
    domains: ['neurobiologia', 'inżynieria', 'materiały'],
    sourceNote: 'Przewodzenie skokowe i rola mieliny są ustalonym mechanizmem neurofizjologicznym.',
    kind: 'bridge',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-open-consciousness-time',
    title: 'Czego nauka nie domyka w subiektywnym czasie',
    hook: 'Potrafimy zmierzyć czas reakcji. Nadal nie mamy pełnego modelu „jak długo to trwało dla mnie”.',
    explanation:
      'Istnieją modele zegarów wewnętrznych, teorii skalowania czasu i kodowania w pamięci. Żaden nie jest kompletnym opisem doświadczenia trwania. To uczciwie otwarte pytanie, nie luka marketingowa.',
    deeper:
      'Rozróżnij: (1) dyskryminację krótkich interwałów, (2) poczucie tempa w chwili, (3) ocenę minionej godziny. Mogą zależeć od różnych obwodów. Łączenie ich w jeden „zegar” jest ruchem teoretycznym, który trzeba uzasadnić.',
    question: 'Którą z tych trzech warstw Twoja hipoteza naprawdę chce wyjaśnić jako pierwszą?',
    domains: ['świadomość', 'neurobiologia', 'epistemologia'],
    sourceNote:
      'Otwartość problemu dotyczy jednolitego mechanizmu doświadczenia czasu, nie samego istnienia efektów nowości czy uwagi.',
    kind: 'open_question',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-analogy-limit',
    title: 'Podobieństwo nie jest mechanizmem',
    hook: 'Dwie rzeczy mogą wyglądać podobnie i nie mieć ze sobą nic wspólnego pod spodem.',
    explanation:
      'Sieć neuronowa, sieć rzeczna i sieć społeczna dzielą słowo „sieć”. To może być użyteczna analogia strukturalna albo tylko metafora. Wspólny mechanizm wymaga wskazania tego samego rodzaju procesu przyczynowego, nie tylko podobnego rysunku.',
    deeper:
      'W laboratorium warto oznaczać relacje: mechanizm / analogia / metafora / słaba zbieżność. Inaczej ładne połączenie zaczyna udawać odkrycie.',
    question: 'Które Twoje ostatnie połączenie jest analogią, a które naprawdę wskazuje ten sam rodzaj procesu?',
    domains: ['epistemologia', 'filozofia'],
    kind: 'bridge',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-kahneman-curiosity',
    title: 'Szybka pewność, wolne pytanie',
    hook: 'Umysł lubi domykać historię. Ciekawość żyje tam, gdzie historia zostaje otwarta.',
    explanation:
      'Kahneman opisuje sprawne, skojarzeniowe wnioskowanie i wolniejszy tryb korygujący. Ciekawość naukowa często polega na tym, by nie pozwolić pierwszemu domknięciu udawać wyjaśnienia. To nie morał — to opis dwóch trybów przetwarzania.',
    deeper:
      '„Pułapki myślenia” nie oznaczają, że intuicja jest bezwartościowa. Intuicja bywa hipotezą. Problem zaczyna się, gdy hipoteza nie zostaje oznaczona.',
    question: 'Jaka Twoja pewność z ostatnich dni zasługuje na degradację z faktu do hipotezy?',
    domains: ['psychologia poznawcza', 'epistemologia'],
    sourceNote: 'Nawiązanie do rozróżnienia szybkiego i wolnego myślenia u Kahnemana — jako ramy, nie jako cytat z książki.',
    kind: 'reading',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-science-faith',
    title: 'Dwa rodzaje pytań, jeden stół',
    hook: 'Nie każde ważne pytanie da się rozstrzygnąć tym samym narzędziem.',
    explanation:
      'Metoda naukowa dobrze testuje twierdzenia empiryczne: da się wskazać, co zaobserwujemy, jeśli są prawdziwe. Pytania metafizyczne i teologiczne mogą być serio rozważane, ale nie stają się empiryczne przez analogię do fizyki. Można je trzymać obok siebie bez udawania, że jedno udowodniło drugie.',
    deeper:
      'Kosmiczne początki, świadomość i sens to miejsca, gdzie łatwo o kategorialne pomieszanie. Aplikacja ma miejsce na wszystkie te warstwy — pod warunkiem, że są nazwane.',
    question: 'Które Twoje pytanie o początek jest empiryczne, a które pozostaje filozoficzne nawet po idealnym eksperymencie?',
    domains: ['filozofia', 'kosmologia', 'nauka i wiara'],
    kind: 'open_question',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-prediction-error',
    title: 'Mózg jako maszyna pomyłek',
    hook: 'Uczenie często zaczyna się nie od bodźca, lecz od różnicy między oczekiwaniem a światem.',
    explanation:
      'W wielu modelach układ nerwowy przewiduje wejście i aktualizuje się, gdy predykcja zawodzi. To nie znaczy, że „żyjesz w symulacji”. Znaczy, że percepcja jest częściowo konstrukcją korygowaną przez błąd.',
    deeper:
      'Błąd predykcji pojawia się w badaniach nagrody, percepcji i motoryki. Czy te „błędy” to jeden mechanizm, czy rodzina podobnych trików obliczeniowych — to wciąż przedmiot sporu.',
    question: 'Jakie zwykłe zjawisko wokół Ciebie jest teraz interesujące głównie dlatego, że złamało oczekiwanie?',
    domains: ['neurobiologia', 'percepcja'],
    sourceNote: 'Ramą jest predictive processing / prediction error — model roboczy, nie jedyna teoria umysłu.',
    kind: 'mechanism',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
  {
    id: 'spark-graphene-limit',
    title: 'Materiał i obietnica',
    hook: 'Grafen jest niezwykły. Nie każdy rysunek z plasterkiem węgla jest jeszcze wynalazkiem.',
    explanation:
      'Grafen ma wysoką przewodność, wytrzymałość i nietypowe własności elektronowe w dwóch wymiarach. Zastosowanie wymaga rozwiązania skali, defektów, ceny i integracji. Między „właściwość istnieje” a „to zadziała w urządzeniu” jest cała inżynieria.',
    deeper:
      'To samo napięcie dotyczy wielu Twoich koncepcji materiałowych: najpierw właściwość, potem mechanizm, potem ograniczenie, potem najmniejszy bezpieczny test pojęciowy — nie procedura laboratoryjna.',
    question: 'Jaka właściwość materiału Cię kusi, zanim jeszcze wiesz, jakie ograniczenie ją ucina?',
    domains: ['materiały', 'inżynieria', 'fizyka'],
    kind: 'fact',
    generatedBy: 'catalog',
    status: 'fresh',
    linkedThoughtIds: [],
  },
];
