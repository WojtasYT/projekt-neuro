import { z } from 'zod';
import {
  AI_MODES,
  EXPORT_FORMAT,
  EXPORT_VERSION,
  PROVENANCES,
  PROJECT_STATUSES,
  READING_STATUSES,
  RELATION_TYPES,
  THOUGHT_STATUSES,
  THOUGHT_TYPES,
  type LabExport,
} from './types';

const thoughtType = z.enum(THOUGHT_TYPES);
const thoughtStatus = z.enum(THOUGHT_STATUSES);
const provenance = z.enum(PROVENANCES);
const relationType = z.enum(RELATION_TYPES);
const projectStatus = z.enum(PROJECT_STATUSES);
const readingStatus = z.enum(READING_STATUSES);
const aiMode = z.enum(AI_MODES);

const hypothesisSchema = z
  .object({
    observation: z.string().optional(),
    proposal: z.string().optional(),
    mechanism: z.string().optional(),
    assumptions: z.string().optional(),
    known: z.string().optional(),
    alternatives: z.string().optional(),
    expectations: z.string().optional(),
    falsifiers: z.string().optional(),
    nextStep: z.string().optional(),
    related: z.string().optional(),
  })
  .optional();

const thoughtSchema = z.object({
  id: z.string().min(1),
  title: z.string(),
  rawText: z.string(),
  content: z.string(),
  types: z.array(thoughtType),
  status: thoughtStatus,
  tags: z.array(z.string()),
  createdAt: z.string(),
  updatedAt: z.string(),
  lastOpenedAt: z.string(),
  provenance: provenance,
  projectIds: z.array(z.string()),
  sourceIds: z.array(z.string()),
  readingItemIds: z.array(z.string()),
  isSample: z.boolean().optional(),
  hypothesis: hypothesisSchema,
  expansions: z.array(
    z.object({
      id: z.string(),
      text: z.string(),
      provenance,
      createdAt: z.string(),
    }),
  ),
});

const projectSchema = z.object({
  id: z.string().min(1),
  title: z.string(),
  purpose: z.string(),
  description: z.string(),
  status: projectStatus,
  nextStep: z.string(),
  createdAt: z.string(),
  updatedAt: z.string(),
  isSample: z.boolean().optional(),
  history: z.array(z.object({ at: z.string(), note: z.string() })),
});

const relationshipSchema = z.object({
  id: z.string().min(1),
  fromId: z.string(),
  toId: z.string(),
  fromKind: z.enum(['thought', 'project', 'reading', 'source', 'concept']),
  toKind: z.enum(['thought', 'project', 'reading', 'source', 'concept']),
  relationType,
  explanation: z.string(),
  provenance,
  confidence: z.enum(['low', 'medium', 'high']).optional(),
  createdAt: z.string(),
});

const sourceSchema = z.object({
  id: z.string().min(1),
  title: z.string(),
  authors: z.string(),
  year: z.string().optional(),
  publisher: z.string().optional(),
  doi: z.string().optional(),
  url: z.string().optional(),
  sourceType: z.enum(['book', 'paper', 'article', 'podcast', 'note', 'other']),
  notes: z.string(),
  accessedAt: z.string().optional(),
  createdAt: z.string(),
});

const readingSchema = z.object({
  id: z.string().min(1),
  title: z.string(),
  author: z.string(),
  status: readingStatus,
  progressNote: z.string(),
  currentPage: z.string(),
  notes: z.string(),
  isStarter: z.boolean().optional(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

const sparkSchema = z.object({
  id: z.string().min(1),
  title: z.string(),
  hook: z.string(),
  explanation: z.string(),
  deeper: z.string().optional(),
  question: z.string(),
  domains: z.array(z.string()),
  sourceNote: z.string().optional(),
  kind: z.enum([
    'observation',
    'fact',
    'open_question',
    'bridge',
    'mechanism',
    'resume',
    'reading',
  ]),
  generatedBy: z.enum(['catalog', 'local', 'ai']),
  status: z.enum(['fresh', 'saved', 'dismissed']),
  linkedThoughtIds: z.array(z.string()),
});

const profileSchema = z.object({
  id: z.literal('local'),
  displayName: z.string(),
  interests: z.array(z.string()),
  currentFocus: z.string(),
  onboarded: z.boolean(),
  theme: z.enum(['dark', 'light']),
  aiDefaultMode: aiMode,
  aiConfig: z.object({
    gatewayUrl: z.string(),
    model: z.string().optional(),
  }),
  remindersEnabled: z.boolean(),
  lastMindState: z
    .enum(['mgła', 'ciekawość', 'mało_energii', 'za_dużo_wątków', 'impuls'])
    .optional(),
  notesToSelf: z.string(),
  updatedAt: z.string(),
});

export const labExportSchema: z.ZodType<LabExport> = z.object({
  format: z.literal(EXPORT_FORMAT),
  version: z.literal(EXPORT_VERSION),
  exportedAt: z.string(),
  thoughts: z.array(thoughtSchema),
  projects: z.array(projectSchema),
  relationships: z.array(relationshipSchema),
  sources: z.array(sourceSchema),
  readingItems: z.array(readingSchema),
  sparks: z.array(sparkSchema),
  profile: profileSchema,
});

export function parseLabExport(input: unknown): LabExport {
  const parsed = labExportSchema.safeParse(input);
  if (!parsed.success) {
    const first = parsed.error.issues[0];
    const path = first?.path.join('.') || 'plik';
    throw new Error(`Niepoprawny plik eksportu (${path}: ${first?.message ?? 'błąd'}).`);
  }
  return parsed.data;
}
