# PROMPT 01 — Make native Android the primary product

Act as a principal Android engineer and migration lead. Use the audit report from Prompt 00 and inspect the current repository again before changing anything.

## Non-negotiable platform decision

The primary deliverable is now a **native Android application**, written in Kotlin with Jetpack Compose. Build a real Android Studio/Gradle project that can compile into an installable APK.

A PWA, browser-installed website, WebView wrapper, Trusted Web Activity, or web page packaged as an app is not an acceptable substitute for the native app. Do not keep adding features to the PWA as if it were the final product.

The existing PWA is a prototype/reference. Preserve it non-destructively until the native app has reached feature parity for the valuable core flows and its data has a migration/export path. Do not delete the PWA or rewrite Git history. If the current repo is a web project at its root, add a clearly named Android project directory such as `android/` rather than destroying the current build setup.

## Resolve the old documentation conflict

The existing `AGENTS.md` and `docs/WIZJA_PRODUKTU.md` may still describe IndexedDB, service workers, and a PWA as the target architecture. Update the project documentation so there is one unambiguous source of truth: **native Android/Kotlin/Compose is the primary product; the existing PWA is a preserved prototype/reference only**. Move or clearly label obsolete PWA-specific implementation requirements as legacy notes. Preserve all product, user-profile, UX, privacy, and scientific-integrity requirements that remain valid. Do not leave contradictory instructions that could cause a later agent to resume building the PWA as the main app.

## Technical target

Use current stable, mutually compatible Android tooling after checking official documentation:
- Kotlin;
- Jetpack Compose for all primary UI;
- Material 3 as a component foundation, with a custom Neurotrino Lab design system;
- Navigation Compose or another appropriate native navigation solution;
- Room/SQLite for durable local data;
- DataStore for settings and preferences;
- Coroutines and Flow for async/reactive state;
- WorkManager only where persistent background work is truly required;
- Android Photo Picker and Storage Access Framework for attachments;
- a repository/use-case architecture with clear separation of UI, domain, data, and integrations.

Do not invent dependency versions. Check the official Android documentation and choose stable versions compatible with the installed Gradle, AGP, Kotlin, JDK, and SDK. Avoid preview/beta dependencies unless a documented requirement makes them necessary.

Keep the architecture pragmatic. Do not create dozens of empty modules or abstractions before they are needed. A clean single Android app module is acceptable initially, with packages/features separated logically. Use dependency injection only if it simplifies the actual project; do not add a framework ceremonially.

## Data migration and preservation

Before implementation, document the current PWA data schema and determine whether there is real user data in the repository, local browser storage, or exported fixtures. Never assume that code access equals access to the user's browser IndexedDB.

Implement:
1. A versioned import/export format for thoughts, projects, concepts, relationships, sources, reading items, profile settings, and attachment metadata.
2. Validation and clear errors on import; never silently discard malformed records.
3. Room migrations with stable IDs and timestamps.
4. Preservation of original raw user-authored thought text. AI-edited or summarized versions must remain separate.
5. A migration path from a PWA JSON export into Android, documented for the user.

Do not fabricate old notes or seed the app with fake personal data. Clearly label optional demo content and make it removable.

## Offline-first behaviour

The app's local core must work without internet:
- create/edit/search/archive thoughts;
- create projects and relationships;
- browse the local knowledge graph/list representation;
- view saved sources/reading items and attachments that have been downloaded;
- export local data.

AI generation, web research, and remote source lookup may require internet. Their offline state must be explicit and graceful. Never show a spinner forever or fake an AI result. Do not silently queue private note content to be sent later; any external transmission must be user-initiated and visible.

## Privacy and AI integration

This is a single-user private application:
- no ads, public profile, social feed, analytics, or telemetry by default;
- no cloud sync by default;
- no API secrets in the Android APK, Git history, Gradle files, or client-side resources;
- use a server-side proxy/backend for paid or secret-bearing AI credentials;
- define provider interfaces so the user can switch model providers later;
- make the exact note/context being sent to an external AI service visible before sending;
- distinguish AI output from the user's original writing;
- if no backend/provider is configured, show a clear configuration state rather than a fake working assistant.

Do not add biometric locks or encryption complexity that is not properly implemented and tested. Do include Android backup/export considerations and document the actual protection boundaries.

## Native Android capabilities

Use real platform integration where it adds value:
- Android share target/intents to capture selected text from other apps, only when the user invokes Share;
- Photo Picker for selected images, diagrams, and screenshots;
- document picker for PDFs/text files;
- local notifications only as an opt-in feature, off by default;
- optional app shortcuts for “Zapisz myśl” and “Wróć do wątku” if they are robust;
- responsive/adaptive Compose layouts for phones and larger screens.

Do not add microphone, voice-chat, or always-listening features. The user prefers writing and optional text dictation through the Android keyboard rather than a voice assistant.

## Delivery phases

Phase 1 — create and build the native Android shell:
- Gradle project opens in Android Studio;
- application ID/package is consistent and documented;
- app launches on emulator/device;
- native navigation and custom theme exist;
- no WebView is used as the primary interface.

Phase 2 — port the core domain experience:
- Home/Laboratorium;
- quick thought capture;
- Thought Studio / detail / editing;
- local Room persistence;
- search;
- projects and typed relationships;
- export/import.

Phase 3 — exploration and research:
- concept map;
- sparks/curiosity engine interface;
- book/reading companion;
- AI provider adapter and secure backend integration point;
- attachments and image/document selection.

Do not fake unfinished Phase 3 functionality to make the screen look complete. Label unavailable features honestly and prioritize a stable Phase 1+2 vertical slice.

## Build and handoff

- Add README instructions for opening/building/running the Android app.
- Add Gradle wrapper if missing and safe to generate.
- Configure a GitHub Actions workflow for Android debug build if the repository already uses CI or if it can be added cleanly.
- Run Gradle build, unit tests, lint, and any available UI/instrumentation tests. Record exact commands and results.
- Generate a debug APK as a build artifact when the environment supports it; otherwise document the exact local build command.
- Preserve the existing PWA prototype and document its status as legacy/reference, not primary product.
- Do not merge the PR. Summarize changed files, decisions, known limitations, and build verification.

Make reasonable decisions without interrupting the user for minor preferences. If there is a genuine blocker, report it clearly and continue with the safe work that is possible.
