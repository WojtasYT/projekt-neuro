import type { Project, Relationship, Thought } from '../domain/types';

const now = '2024-01-01T12:00:00.000Z';

export const SAMPLE_PROJECT: Project = {
  id: 'sample-project-neurotrino',
  title: 'Neurotrino',
  purpose: 'Środowisko do nieliniowego łączenia myśli, bez oddawania autorstwa.',
  description:
    'Przykładowy projekt startowy. Możesz go zostawić, przepisać albo usunąć. To kontener na Twoje rzeczywiste notatki — nie gotowa teoria.',
  status: 'incubating',
  nextStep: 'Zapisać jedną surową obserwację, która ostatnio została w głowie.',
  createdAt: now,
  updatedAt: now,
  isSample: true,
  history: [{ at: now, note: 'Utworzono jako treść przykładową.' }],
};

export const SAMPLE_THOUGHTS: Thought[] = [
  {
    id: 'sample-thought-clock',
    title: 'Zegar neuronalny — ziarno',
    rawText:
      'A gdyby subiektywne poczucie upływu czasu zależało od liczby i rodzaju zdarzeń neuronalnych, nowości, rutyny i tła neurochemicznego? To nie jest twierdzenie. To pytanie, które chcę utrzymać przy życiu.',
    content:
      'A gdyby subiektywne poczucie upływu czasu zależało od liczby i rodzaju zdarzeń neuronalnych, nowości, rutyny i tła neurochemicznego? To nie jest twierdzenie. To pytanie, które chcę utrzymać przy życiu.',
    types: ['hypothesis', 'question'],
    status: 'paused',
    tags: ['czas', 'neurony', 'nowość'],
    createdAt: now,
    updatedAt: now,
    lastOpenedAt: now,
    provenance: 'sample',
    projectIds: [],
    sourceIds: [],
    readingItemIds: [],
    isSample: true,
    hypothesis: {
      observation: 'Godzina w nowym miejscu potrafi być „grubsza” niż godzina w rutynie.',
      proposal:
        'Subiektywne tempo czasu może skalować się z gęstością i rodzajem zdarzeń neuronalnych, nie z samym czasem zegarowym.',
      assumptions: 'Że istnieje mierzalny odpowiednik „zdarzenia” i że da się je odróżnić od nowości i uwagi.',
      known: 'Efekty nowości i zniekształcenia retrospektywnego czasu są opisywane; jednolity zegar zdarzeń — nie.',
      nextStep: 'Rozdzielić trzy warstwy: chwila, tempo, ocena wsteczna. Zająć się jedną.',
    },
    expansions: [],
  },
  {
    id: 'sample-thought-lab',
    title: 'Po co to laboratorium',
    rawText:
      'Chcę miejsce, w którym mogę złapać myśl zanim zniknie, wrócić do niej bez wstydu i połączyć ją z inną — nawet jeśli nic z tego nie będzie „dziełem”.',
    content:
      'Chcę miejsce, w którym mogę złapać myśl zanim zniknie, wrócić do niej bez wstydu i połączyć ją z inną — nawet jeśli nic z tego nie będzie „dziełem”.',
    types: ['reflection', 'project_note'],
    status: 'seed',
    tags: ['neurotrino', 'proces'],
    createdAt: now,
    updatedAt: now,
    lastOpenedAt: now,
    provenance: 'sample',
    projectIds: ['sample-project-neurotrino'],
    sourceIds: [],
    readingItemIds: [],
    isSample: true,
    expansions: [],
  },
];

export const SAMPLE_RELATIONSHIPS: Relationship[] = [
  {
    id: 'sample-rel-lab-project',
    fromId: 'sample-thought-lab',
    toId: 'sample-project-neurotrino',
    fromKind: 'thought',
    toKind: 'project',
    relationType: 'belongs_to_project',
    explanation: 'Notatka o sensie laboratorium należy do projektu Neurotrino.',
    provenance: 'sample',
    createdAt: now,
  },
];
