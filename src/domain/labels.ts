import type {
  AiMode,
  MindState,
  ProjectStatus,
  Provenance,
  ReadingStatus,
  RelationType,
  ThoughtStatus,
  ThoughtType,
} from './types';

export const thoughtTypeLabels: Record<ThoughtType, string> = {
  observation: 'obserwacja',
  question: 'pytanie',
  hypothesis: 'hipoteza',
  model: 'model',
  analogy: 'analogia',
  invention: 'wynalazek',
  reflection: 'refleksja',
  project_note: 'notatka projektu',
  reading_note: 'notatka z lektury',
  other: 'inne',
};

export const thoughtStatusLabels: Record<ThoughtStatus, string> = {
  seed: 'ziarno',
  exploring: 'eksploracja',
  paused: 'wstrzymana',
  connected: 'połączona',
  checking: 'sprawdzanie',
  developed: 'rozwinięta',
  archived: 'archiwum',
};

export const relationTypeLabels: Record<RelationType, string> = {
  supports: 'wspiera',
  contradicts: 'kwestionuje',
  extends: 'rozszerza',
  analogy: 'analogia',
  shared_mechanism: 'wspólny mechanizm',
  inspired_by: 'zainspirowane',
  source_for: 'źródło dla',
  belongs_to_project: 'należy do projektu',
  metaphor: 'metafora',
  weak_association: 'słaba zbieżność',
};

export const relationTypeHints: Record<RelationType, string> = {
  supports: 'Jedna myśl wzmacnia drugą, ale nie przesądza o prawdziwości.',
  contradicts: 'Napięcie albo sprzeczność — warto je zachować, nie rozstrzygać na siłę.',
  extends: 'Druga myśl rozwija lub doprecyzowuje pierwszą.',
  analogy: 'Podobieństwo strukturalne; nie implikuje wspólnego mechanizmu.',
  shared_mechanism: 'Propozycja, że pod spodem działa ten sam rodzaj procesu.',
  inspired_by: 'Jedna myśl pojawiła się dzięki drugiej.',
  source_for: 'Materiał źródłowy albo lektura, z której wyrosła myśl.',
  belongs_to_project: 'Myśl należy do dłuższego wątku projektowego.',
  metaphor: 'Obraz pomocniczy, nie opis fizyczny.',
  weak_association: 'Luźne skojarzenie; świadomie słabe.',
};

export const projectStatusLabels: Record<ProjectStatus, string> = {
  active: 'w toku',
  paused: 'wstrzymany',
  incubating: 'inkubacja',
  archived: 'archiwum',
};

export const readingStatusLabels: Record<ReadingStatus, string> = {
  unopened: 'jeszcze nie ruszona',
  exploring: 'w trakcie',
  paused: 'odłożona',
  finished: 'przeczytana',
};

export const provenanceLabels: Record<Provenance, string> = {
  user: 'Twoje',
  ai: 'AI',
  source: 'źródło',
  joint: 'wspólne',
  sample: 'przykład',
};

export const mindStateLabels: Record<MindState, string> = {
  mgła: 'mam mgłę',
  ciekawość: 'mam ciekawość',
  mało_energii: 'mam mało energii',
  za_dużo_wątków: 'mam za dużo wątków',
  impuls: 'mam impuls',
};

export const mindStateHints: Record<MindState, string> = {
  mgła: 'Jedno niedokończone zdanie wystarczy. Nie musisz wiedzieć, dokąd to zmierza.',
  ciekawość: 'Zapisz, co Cię złapało — nawet jeśli to tylko krawędź zjawiska.',
  mało_energii: 'Fragment bez obowiązku kontynuacji. Sam zapis jest ruchem.',
  za_dużo_wątków: 'Zrzuć je wszystkie, bez sortowania. Oddzielaj pustą linią.',
  impuls: 'Łap to, zanim umknie. Tytuł i kategorie mogą poczekać.',
};

export const aiModeLabels: Record<AiMode, string> = {
  iskra: 'Iskra',
  sokrates: 'Sokrates',
  lacznik: 'Łącznik',
  adwokat: 'Adwokat diabła',
  weryfikator: 'Weryfikator',
  rozwiniecie: 'Rozwinięcie',
  powrot: 'Powrót do wątku',
};

export const aiModeHints: Record<AiMode, string> = {
  iskra: 'Jeden mały impuls. Minimum słów.',
  sokrates: 'Jedno pytanie. Bez podpowiadania odpowiedzi.',
  lacznik: 'Jedno lub dwa połączenia, z klasą relacji.',
  adwokat: 'Najsilniejsze alternatywne wyjaśnienie.',
  weryfikator: 'Oddziela fakt, konsensus, hipotezę i lukę.',
  rozwiniecie: 'Głębsza analiza — tylko gdy o nią prosisz.',
  powrot: 'Krótki punkt wznowienia z Twojej notatki.',
};

export const defaultInterests = [
  'neurobiologia',
  'neurochemia',
  'percepcja',
  'świadomość',
  'fizyka kwantowa',
  'kosmologia',
  'biologia',
  'inżynieria',
  'filozofia',
  'epistemologia',
  'nauka i wiara',
];
