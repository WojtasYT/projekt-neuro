# PROMPT 04 — Make the core experience genuinely functional and verify it

Act as a senior product engineer and QA lead. Use the current native Android implementation. Your task is to replace any remaining mockups, placeholder interactions, and superficial feature shells with a stable, coherent, testable product slice.

## Product principle

Neurotrino Lab is a private research and creativity environment. It exists to help the user notice, capture, explore, connect, validate, and resume their own thoughts. It is not a conventional productivity system. The user should normally contribute 90–99% of the original intellectual content; AI should provide small prompts, factual context, alternative views, and critique rather than doing the entire creative task.

## Required working flows

### A. Capture and Thought Studio
- A thought can be created from one action without onboarding or metadata requirements.
- Autosave is reliable and the save state is clear.
- Raw original text is immutable in provenance; later edits/AI suggestions are separate revisions or attributed additions.
- A thought can be edited, searched, tagged, archived, restored, linked, and attached to a project.
- It can remain an incomplete fragment forever. No mandatory completion checklist.

### B. Relationships and concept graph
- Create/edit/delete relationships between actual stored thoughts/concepts.
- Give relations useful types: “rozwija”, “kwestionuje”, “ma wspólny mechanizm z”, “jest analogią do”, “zainspirowało”, “pochodzi ze źródła”.
- Store provenance and optional confidence/explanation.
- Graph view and relationship list must reflect the same data.
- Never create graph edges from an AI guess without user acceptance.

### C. Projects and reading library
- Projects contain a purpose/question and linked thoughts, sources, attachments, and an optional next step.
- Library supports the user's real books and saved articles, progress notes if desired, and linking reading notes to thoughts.
- Do not force page counts, schedules, streaks, or daily reading goals.

### D. Return-to-thought and low-friction restart
Create a genuinely useful restart path for when the user feels blocked, mentally foggy, worried that an idea is already known, or overwhelmed by too many possibilities:
- offer one small entry point rather than a long menu;
- preserve multiple possible directions without forcing an immediate choice;
- show a brief, accurate reminder of where a thought stopped;
- never shame the user for time away;
- never equate a long pause with failure;
- allow the user to simply write one sentence and stop.

### E. Curiosity sparks
- Show one or a few high-quality prompts, not a low-quality infinite feed.
- Sparks must be grounded in a real mechanism, fact, open question, user's saved thought, or source.
- If a factual statement is made, provide a source or clearly mark that it has not yet been verified.
- Explain the connection to the user's interests or saved notes in concrete terms.
- Allow saving, dismissing, changing angle, or asking for sources.
- Avoid repeated generic trivia and manufactured “unsolved mysteries”.

### F. AI and evidence checking
- Keep AI behind provider interfaces. A model provider must be configurable without rewriting Thought Studio.
- Never embed API keys or privileged secrets in the APK or source repository.
- If no backend is configured, display a truthful disabled/configuration state. Do not simulate an answer.
- Before sending personal notes to an external service, show what content will be sent and require a user action.
- Preserve separate provenance for user writing, AI proposals, and source excerpts.
- Default AI interaction should be low-intervention: one question, one factual correction, one connection candidate, or one small impulse.
- Deeper analysis is opt-in.
- Distinguish established fact, scientific consensus, active research, plausible interpretation, user hypothesis, analogy, metaphor, speculation, and unknown.
- Novelty checking must be cautious. It may report “similar work found”, “known mechanism”, “related concept”, or “no close match found in this limited search”. It must never claim that a thought is definitely original or definitely a scientific breakthrough.
- Cite real sources. Never invent papers, researchers, DOIs, quotations, or experimental findings.

### G. Import, export, and offline reliability
- Versioned JSON export/import with schema validation and an intelligible error summary.
- Include thoughts, relationships, projects, sources, reading items, settings, and attachment metadata.
- Keep core local workflows usable offline.
- Clearly mark network-dependent features and recover gracefully when connectivity returns.
- Do not silently transmit queued private data later.
- Test process death/restart, app relaunch, and database migration paths.

### H. Attachments and Android integration
- Use the Android Photo Picker for selected images; request only the permissions needed.
- Use the system document picker for PDFs and other user-selected files.
- Support Android Share intents for text only when the user explicitly shares content to the app.
- Store references safely and handle revoked/missing file permissions gracefully.
- Do not introduce always-on microphone access, background recording, or voice-chat features.

## What must not be added

- Punitive streaks, shame prompts, overdue task lists, productivity grades, or compulsory check-ins.
- Public feeds, leaderboards, followers, or social comparison.
- Fake AI responses, fake source citations, fake research or “simulated” buttons presented as real.
- Automatic mass upload of private notes to cloud services.
- Unsafe medical, chemical, biological, or self-experimentation instructions.

## Testing requirements

Add meaningful unit/UI tests for at least:
1. create/edit/reopen thought and verify persistence;
2. raw text/provenance preservation;
3. create and remove a typed relationship;
4. search thoughts;
5. create project and attach a thought;
6. export/import round trip;
7. offline access to core notes;
8. AI disabled/unconfigured state;
9. error state when an attachment becomes unavailable;
10. navigation and quick capture on a phone-sized layout.

Run Gradle build, unit tests, lint, and UI tests where supported. Do not claim a test passed unless it actually ran and passed. Report skipped tests and why.

## Definition of done

The app is ready for review only when:
- it builds as a native Android app;
- core thoughts persist locally and work offline;
- no primary action is a dead button;
- the Polish UI has been language-reviewed;
- the native visual design matches the research-lab direction;
- AI and online features fail honestly and securely when unconfigured/offline;
- the user can export their data;
- tests and build results are documented;
- the PWA prototype remains preserved until the user explicitly approves its removal.

Do not merge the pull request. Provide a clear handoff with APK/artifact location if generated, exact build commands, screenshots, test results, remaining limitations, and a file-by-file summary.
