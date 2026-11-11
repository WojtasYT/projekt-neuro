# PROMPT 00 — Repository and product audit (NO CODE CHANGES)

You are acting as a senior Android architect, product engineer, UX researcher, and code auditor. You have access to the current GitHub branch/worktree of `WojtasYT/projekt-neuro`.

## Mission

Audit the application that currently exists in this branch. The user reports that the first build is a PWA, its Polish interface sounds machine-translated and vague, and the visual design is too minimalist and does not communicate the intended immersive research-laboratory experience.

**Do not modify, delete, rename, reformat, or generate application files in this task. Do not commit, push, open a PR, or merge anything.** This is a read-only audit. Inspect the actual repository, not just the brief.

## Context and correction of the original brief

The previous product brief asked for a mobile-first private research environment and also explicitly described `IndexedDB`, a service worker, app-shell caching, responsive web layouts, and desktop support. That made a PWA a reasonable interpretation. However, the platform decision was insufficiently explicit. The user now prefers a **genuine native Android application**. Treat Android native as the target product unless the repository reveals a concrete blocker that must be reported. Do not assume the PWA is the final platform.

The existing PWA is a prototype and a source of reusable product logic, content, data structures, and visual experiments. It must not be discarded before those assets are inventoried.

## Inspect

1. Repository structure, branches/worktree status, README, `AGENTS.md`, product docs, package manifests, build configuration, and all app entry points.
2. Exact stack and architecture: framework, language, routing/navigation, state, persistence, service worker, PWA manifest, API integrations, styling, graph implementation, tests, deployment setup.
3. Every visible screen and major user flow. Identify what is fully implemented, partly implemented, mocked, hard-coded, or inert.
4. The complete user-facing Polish copy. Identify awkward, vague, translated, inconsistent, or meaningless labels and sentences. Quote representative examples with file paths and line numbers.
5. Visual design: hierarchy, density, spacing, typography, navigation, contrast, interaction states, empty states, graph visibility, mobile ergonomics, and whether the screens resemble a generic minimalist dashboard rather than a distinctive personal research environment.
6. Data model and persistence: where notes, ideas, graph relations, projects, sources, attachments, and settings are stored; whether data survives reload/reinstall; export/import; offline behaviour; privacy and external data transmission.
7. AI integration: provider, prompt construction, API key handling, server/client boundaries, error handling, whether suggestions are actually generated or only mocked, provenance, and whether user content is sent without explicit action.
8. Build/test status. Run only safe read-only validation commands where possible; do not alter lockfiles or generated files. Record commands and actual outputs.

## Native Android feasibility assessment

Assess migration to a Kotlin + Jetpack Compose application. Consider:
- Room/SQLite as the local source of truth;
- DataStore for preferences;
- Kotlin Coroutines/Flow;
- WorkManager only for genuinely persistent background jobs;
- Android Photo Picker and Storage Access Framework for user-selected files;
- secure separation between Android client and AI/research backend;
- backup/export and migration of existing PWA data.

Do not add an Android project yet. Determine which existing domain logic/data structures can be reused conceptually or shared via API/schema, and which frontend code cannot be reused directly.

## Required report

Return a concise but specific report with these sections:

A. **What exists** — verified stack, folder map, screens, functions, storage, AI integration.
B. **What actually works** — working / partial / mock / broken table, with evidence.
C. **Polish-language defects** — at least 15 examples when available; current string, why it fails, suggested natural Polish replacement. Do not invent examples absent from the repository.
D. **Visual/UX diagnosis** — exact screens/components and changes required.
E. **Native Android migration** — recommended target architecture, proposed folder/module structure, data migration strategy, dependencies, build risks, and a staged implementation plan.
F. **Data and privacy risks** — concrete issues and priorities.
G. **Acceptance checklist** — measurable criteria for declaring the Android version complete.

Do not start implementation after the report. Do not ask the user generic questions. If a decision cannot be verified, mark it as unknown and explain how to verify it. The report must be based on the actual checked-out branch, not assumptions.
