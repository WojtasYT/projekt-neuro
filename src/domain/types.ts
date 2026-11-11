export const THOUGHT_TYPES = [
  'observation',
  'question',
  'hypothesis',
  'model',
  'analogy',
  'invention',
  'reflection',
  'project_note',
  'reading_note',
  'other',
] as const;

export type ThoughtType = (typeof THOUGHT_TYPES)[number];

export const THOUGHT_STATUSES = [
  'seed',
  'exploring',
  'paused',
  'connected',
  'checking',
  'developed',
  'archived',
] as const;

export type ThoughtStatus = (typeof THOUGHT_STATUSES)[number];

export const PROVENANCES = ['user', 'ai', 'source', 'joint', 'sample'] as const;
export type Provenance = (typeof PROVENANCES)[number];

export const RELATION_TYPES = [
  'supports',
  'contradicts',
  'extends',
  'analogy',
  'shared_mechanism',
  'inspired_by',
  'source_for',
  'belongs_to_project',
  'metaphor',
  'weak_association',
] as const;

export type RelationType = (typeof RELATION_TYPES)[number];

export const PROJECT_STATUSES = ['active', 'paused', 'incubating', 'archived'] as const;
export type ProjectStatus = (typeof PROJECT_STATUSES)[number];

export const READING_STATUSES = ['unopened', 'exploring', 'paused', 'finished'] as const;
export type ReadingStatus = (typeof READING_STATUSES)[number];

export const MIND_STATES = ['mgła', 'ciekawość', 'mało_energii', 'za_dużo_wątków', 'impuls'] as const;
export type MindState = (typeof MIND_STATES)[number];

export const AI_MODES = [
  'iskra',
  'sokrates',
  'lacznik',
  'adwokat',
  'weryfikator',
  'rozwiniecie',
  'powrot',
] as const;

export type AiMode = (typeof AI_MODES)[number];

export const EXPORT_FORMAT = 'neurotrino-lab';
export const EXPORT_VERSION = 1;

export interface HypothesisFields {
  observation?: string;
  proposal?: string;
  mechanism?: string;
  assumptions?: string;
  known?: string;
  alternatives?: string;
  expectations?: string;
  falsifiers?: string;
  nextStep?: string;
  related?: string;
}

export interface Expansion {
  id: string;
  text: string;
  provenance: Provenance;
  createdAt: string;
}

export interface Thought {
  id: string;
  title: string;
  rawText: string;
  content: string;
  types: ThoughtType[];
  status: ThoughtStatus;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  lastOpenedAt: string;
  provenance: Provenance;
  projectIds: string[];
  sourceIds: string[];
  readingItemIds: string[];
  isSample?: boolean;
  hypothesis?: HypothesisFields;
  expansions: Expansion[];
}

export interface Project {
  id: string;
  title: string;
  purpose: string;
  description: string;
  status: ProjectStatus;
  nextStep: string;
  createdAt: string;
  updatedAt: string;
  isSample?: boolean;
  history: Array<{ at: string; note: string }>;
}

export interface Relationship {
  id: string;
  fromId: string;
  toId: string;
  fromKind: GraphNodeKind;
  toKind: GraphNodeKind;
  relationType: RelationType;
  explanation: string;
  provenance: Provenance;
  confidence?: 'low' | 'medium' | 'high';
  createdAt: string;
}

export type GraphNodeKind = 'thought' | 'project' | 'reading' | 'source' | 'concept';

export interface Source {
  id: string;
  title: string;
  authors: string;
  year?: string;
  publisher?: string;
  doi?: string;
  url?: string;
  sourceType: 'book' | 'paper' | 'article' | 'podcast' | 'note' | 'other';
  notes: string;
  accessedAt?: string;
  createdAt: string;
}

export interface ReadingItem {
  id: string;
  title: string;
  author: string;
  status: ReadingStatus;
  progressNote: string;
  currentPage: string;
  notes: string;
  isStarter?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Spark {
  id: string;
  title: string;
  hook: string;
  explanation: string;
  deeper?: string;
  question: string;
  domains: string[];
  sourceNote?: string;
  kind:
    | 'observation'
    | 'fact'
    | 'open_question'
    | 'bridge'
    | 'mechanism'
    | 'resume'
    | 'reading';
  generatedBy: 'catalog' | 'local' | 'ai';
  status: 'fresh' | 'saved' | 'dismissed';
  linkedThoughtIds: string[];
}

export interface AttachmentMeta {
  id: string;
  thoughtId: string;
  name: string;
  mime: string;
  createdAt: string;
  size: number;
}

export interface AttachmentRecord extends AttachmentMeta {
  blob: Blob;
}

export interface AiConfig {
  gatewayUrl: string;
  model?: string;
}

export interface UserProfile {
  id: 'local';
  displayName: string;
  interests: string[];
  currentFocus: string;
  onboarded: boolean;
  theme: 'dark' | 'light';
  aiDefaultMode: AiMode;
  aiConfig: AiConfig;
  remindersEnabled: boolean;
  lastMindState?: MindState;
  notesToSelf: string;
  updatedAt: string;
}

export interface LabExport {
  format: typeof EXPORT_FORMAT;
  version: typeof EXPORT_VERSION;
  exportedAt: string;
  thoughts: Thought[];
  projects: Project[];
  relationships: Relationship[];
  sources: Source[];
  readingItems: ReadingItem[];
  sparks: Spark[];
  profile: UserProfile;
  attachments?: never;
}

export type SaveStatus = 'idle' | 'saving' | 'saved' | 'error';
