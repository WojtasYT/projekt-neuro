# AGENTS.md — NEUROTRINO LAB / Personal Research Environment

> Read this file and `docs/PROFIL_UZYTKOWNIKA.md` plus `docs/WIZJA_PRODUKTU.md` before making product or architecture decisions. These files are the project brief. The repository is the source of truth for implementation; this brief is the source of truth for product intent.

## 1. Role and mission

Act as a senior product engineer, product designer, interaction designer, and pragmatic technical lead. Build a polished, genuinely usable, private, Polish-first, mobile-first personal research and creativity environment. The working name is **Neurotrino Lab**. The existing repository may be named `projekt-neuro`; do not rename it or replace its history without explicit permission.

This is not a generic to-do list, habit tracker, Pomodoro clone, school-learning platform, chatbot wrapper, or gamified productivity dashboard. Its central purpose is to help the owner recover and deepen a distinctive process of curiosity: noticing something, asking why, exploring mechanisms, connecting distant disciplines, forming a personal hypothesis, checking it against reality, and returning to the thought later.

The product should reduce the friction between *"I want to think about this"* and *actually starting*, without shaming, coercing, infantilising, or replacing the user's thinking.

## 2. Non-negotiable product principles

1. **The user is the primary thinker and author.** Default AI contribution is small: a fact, a question, a contrast, a connection candidate, or a short impulse. The user wants to contribute roughly 90–99% of the original thinking in normal sessions. Larger AI analyses are available only when explicitly requested.
2. **Curiosity before productivity.** The product is successful when it helps a thought begin, develop, connect, or resume—not when it maximises screen time, streaks, points, tasks, or reading quotas.
3. **Low-friction entry.** A user must be able to open the app and capture or explore one thought immediately, without completing onboarding, choosing among a large menu, or filling a form.
4. **Progressive depth.** Start with a clear, interesting explanation or a small prompt. Offer deeper mechanisms, technical nuance, sources, objections, and details on demand. Never dump an encyclopaedia on the user by default.
5. **Non-binary thinking.** Allow several explanations, partial confidence, ambiguity, mixed preferences, and unresolved tensions. Avoid forcing a single choice when "both", "depends", "not sure", or "something else" better captures reality.
6. **No school-like tone.** No generic homework framing, empty motivational slogans, patronising praise, rigid daily curriculum, or simplistic self-help language. Use vivid, precise, natural Polish.
7. **Respect the user's originality concern.** Finding that an idea is already known must not be framed as failure. Distinguish personal rediscovery, a known mechanism, a new combination, an untested extension, and a potentially novel claim. Never promise that an idea is a scientific breakthrough.
8. **Scientific honesty.** Distinguish established evidence, current consensus, plausible interpretation, hypothesis, analogy, metaphor, speculation, and unknown. Cite sources for factual claims when research tools are available. Never invent references, results, DOIs, or experimental findings.
9. **Privacy by default.** This is a single-user personal environment. Keep notes and personal ideas local by default. No telemetry, advertising, public profile, social feed, or cloud sync unless explicitly requested. Never commit secrets or private notes to an external service without clear user intent.
10. **No guilt loops.** No punitive streak loss, red failure states, shame copy, compulsory check-ins, or repeated nagging. Reminders and notifications are off unless explicitly enabled.
11. **User controls the AI.** Never silently send all personal notes to an AI provider. Make the content being sent clear and require an explicit action. Provide a clear indication when AI or online research is unavailable.
12. **Preserve unfinished thoughts.** Notes can remain fragments, contradictions, sketches, or open questions. A thought does not need to become a finished theory or project to be valuable.
13. **No false attribution.** Mark provenance: user-authored text, AI suggestion, imported/source material, and jointly edited text must be distinguishable. Never rewrite the user's raw original capture without preserving it.
14. **Safety and epistemic boundaries.** This is an educational and conceptual research tool, not a medical, chemical, biological, or engineering procedure generator. Do not produce actionable instructions for dangerous chemical synthesis, toxic substances, drug use, unsupervised self-experimentation, risky biohacking, weapons, or unsafe experiments. Offer safe conceptual explanations, literature context, simulations, or appropriately supervised high-level alternatives.

## 3. Product identity and language

- All user-facing UI, onboarding, empty states, labels, helper text, errors, and default AI interactions must be in **Polish**.
- Code identifiers, comments, type names, and technical documentation may be in English.
- Visual direction: a refined futuristic research laboratory combined with an explorable knowledge universe and a personal project control room.
- The interface should feel intelligent, atmospheric, and inviting—not like a neon-heavy game UI. Prefer a restrained dark theme with carefully used luminous accents, excellent typography, hierarchy, spatial clarity, and a useful light theme if feasible.
- Make mobile/Android use the first-class layout, but ensure desktop is excellent for longer thinking and graph exploration.
- Avoid excessive cards, dashboards full of metrics, visual clutter, and decorative motion that competes with reading.

## 4. Required first-session behaviour

Do not force the user through a long questionnaire. On first use:
- Show a short, skippable introduction explaining that this is a place to capture, explore, connect, and resume thoughts.
- Offer immediate actions such as **Zapisz myśl**, **Wróć do wątku**, and **Zaskocz mnie**.
- Let the user edit their profile/preferences later.
- Preferences may be uncertain or context-dependent. Never force a permanent ranking of interests.
- Provide useful sample/demo content only if clearly labelled as sample and removable.

## 5. Core experience and product modules

Build a coherent product, not a collection of disconnected demo screens. Implement a strong vertical slice first, then extend it. The following capabilities define the intended product.

### 5.1 Home / Laboratory console

A calm, highly legible home screen that answers: *What can I do right now without having to plan?*

Prioritise:
- Resume the most recent unfinished thought.
- Quick-capture a raw idea in one action.
- Start a short exploratory session with one small prompt.
- See one or a few personally relevant sparks—not an endless feed.
- Return to an active project or a saved question.
- Optional, skippable state selection such as `mam mgłę`, `mam ciekawość`, `mam mało energii`, `mam za dużo wątków`, `mam impuls`. This is contextual input, not a diagnosis or a personality classification.

The home screen must not demand a daily commitment. Never show a failure state because the user has not opened the app.

### 5.2 Thought capture and Thought Studio (highest priority)

This is the heart of the product.

Support:
- One-click capture of a fragment, observation, question, analogy, quote/reference, or possible hypothesis.
- A frictionless free-writing canvas. Autosave safely; show save status.
- Optional conversion of a raw seed into a more structured thought.
- Thought types that can overlap: observation, question, hypothesis, model, analogy, invention, reflection, project note, reading note, unknown/other.
- Thought states that are not a judgement: seed, exploring, paused, connected, checking evidence, developed, archived. Allow changing status freely.
- A clear distinction between the original raw note and later revisions/AI-assisted expansions.
- Attachments or references to images, diagrams, and notes where practical.
- Links to other thoughts, projects, sources, and reading items.
- Search, tags, filters, and a recent activity view.
- No forced completion checklist. Structure is optional and progressively disclosed.

For a hypothesis, optionally offer fields such as:
1. What did I notice?
2. What am I proposing?
3. What mechanism could explain it?
4. Which parts are assumptions?
5. What is already known?
6. What alternative explanations exist?
7. What would I expect to observe if this were true?
8. What evidence would weaken or falsify it?
9. What safe next step could clarify it?
10. Which sources or prior ideas are related?

Do not make the user fill all ten fields. The user can leave any field empty indefinitely.

### 5.3 Curiosity engine / Sparks

Generate occasional, relevant entry points that reconnect curiosity to real mechanisms and the user's existing interests.

Spark types:
- An everyday observation with a hidden scientific mechanism.
- A surprising but verifiable fact.
- An unresolved scientific question.
- Two concepts from distant fields that may share a mechanism.
- A known mechanism with an unusual application.
- A continuation of one of the user's own unfinished thoughts.
- A question emerging from a book, podcast, image, or prior conversation.

Each spark should normally contain:
- A short hook (not clickbait).
- A clear, accessible explanation.
- One optional deeper layer.
- A factual/source panel when the claim is factual.
- One open question that invites the user to think.
- A control to save, dismiss, explore, or ask for a different angle.

Generate one strong spark rather than ten mediocre suggestions. Avoid repetitive trivia and generic "Did you know?" content. Do not present fabricated open problems as unresolved science.

### 5.4 Cross-domain bridge engine

Offer carefully chosen combinations across biology, neuroscience, neurochemistry, psychology, perception, physics, cosmology, chemistry, materials, engineering, philosophy, epistemology, and science-faith questions.

A bridge must state what kind of relationship is being proposed:
- shared mechanism;
- structural similarity;
- useful analogy;
- metaphor only;
- possible causal connection requiring evidence;
- coincidence / weak association.

Never imply that similarity proves a shared physical mechanism. Let the user accept, reject, modify, or save a candidate connection. Prioritise links to the user's existing thoughts over arbitrary random combinations.

### 5.5 Knowledge graph / Concept universe

Provide an interactive, usable map connecting thoughts, questions, hypotheses, projects, sources, and concepts.

Required behaviour:
- Create/edit/delete nodes and links.
- Label the meaning of each connection (supports, contradicts, extends, analogy, shared mechanism, inspired by, source for, belongs to project).
- Navigate from a node to its content.
- Search/filter and focus on a local neighbourhood so large graphs remain usable.
- Distinguish user-authored links from AI-proposed links.
- Do not invent connections silently. AI suggestions require acceptance.
- On small screens provide a readable list/relationship view as an alternative to a dense graph.

Do not let the graph become a decorative visual with no functional persistence.

### 5.6 Projects and invention incubator

Support long-running projects such as Neurotrino, future inventions, engineering concepts, and creative research threads.

Each project can hold:
- purpose / current question;
- status and next optional action;
- notes and linked thoughts;
- sketches/images/files when feasible;
- related sources;
- milestones or experiments only when useful;
- a change/history log that preserves earlier thinking.

A project must support a next tiny action, but it must not turn into a rigid task manager. Do not force deadlines.

### 5.7 AI thinking partner

The assistant should have explicit interaction modes, selectable when useful:

- **Iskra** — one subtle question or one small stimulus; minimum AI contribution.
- **Sokrates** — ask a focused follow-up question; do not reveal the likely answer immediately.
- **Łącznik** — suggest one or two possible cross-domain connections and explain whether each is a mechanism, analogy, or speculation.
- **Adwokat diabła** — challenge assumptions fairly and show the strongest alternative explanation.
- **Weryfikator** — check factual claims against reliable sources, expose uncertainty, and separate evidence levels.
- **Rozwinięcie na życzenie** — a deeper explanation or analysis, only when requested.
- **Powrót do wątku** — use the user's own saved thoughts to help resume without recapping everything.

Default behaviour: do not generate a finished theory on the user's behalf. Ask one useful question at a time. Do not over-question. Use the user's words, preserve their ownership, and explicitly mark AI-authored additions.

If the user is stuck, first reduce the next action to something very small (for example: describe one observation, name one uncertainty, or choose which of two fragments feels more interesting). Do not give a motivational speech.

### 5.8 Research and evidence workspace

When browsing/search integrations are available, help investigate claims and possible originality. For every meaningful source retain title, authors, publication date/year, journal/publisher, DOI or URL, source type, short notes, and the claim it supports or challenges.

For research results:
- Prefer primary papers, systematic reviews, authoritative institutional sources, and high-quality reference material.
- State the date of the search and the scope of what was searched when assessing novelty.
- Distinguish what a source actually demonstrates from what it merely discusses.
- Never fabricate a citation, quote, paper, author, DOI, or database search.
- Make it easy to return from a source to the thought it relates to.

**Novelty checking is not a novelty certificate.** If a similar idea is found, explain the relationship and preserve the personal discovery. If nothing similar is found, state that the search was limited and cannot prove originality. Separate:
1. new to the user;
2. a rediscovery of an established idea;
3. a known mechanism applied in a different context;
4. a new combination of known elements;
5. a potentially novel, testable extension;
6. genuinely unknown / not assessed.

### 5.9 Reading companion

Initial personal library includes:
- *Kwantechizm 2.0* — Andrzej Dragan.
- *Krótka historia czasu. Ilustrowana* — Stephen Hawking.
- *Wszechświat w skorupce orzecha* — Stephen Hawking.
- *Psychodeliki* — David Nutt.
- *Neuromania* — Albert Moukheiber.
- *Pułapki myślenia* — Daniel Kahneman.
- *DOSE* — TJ Power (the user reports having completed it recently).

The owner has partially explored *Neuromania* and *Wszechświat w skorupce orzecha*. Do not imply they have finished the other books.

The reading companion should:
- Let the user add a book and optionally record current progress.
- Accept personal notes, page/section references, and short excerpts supplied by the user.
- Connect a reading note with their hypotheses/projects.
- Suggest context-relevant reading moments without imposing quotas.
- Offer a small question or connection after a user-provided excerpt.
- Never reproduce large copyrighted passages or retrieve pirated content.
- Not assume the user will read daily. Reading is one possible path into the subject, not a moral obligation.

### 5.10 Personal memory and adaptation

Maintain an editable, inspectable user profile with interests, current projects, interaction preferences, recurring concepts, and corrections.

Rules:
- Store only information useful to the product and explicitly provided or confirmed by the user.
- Let the user inspect, edit, export, and delete memory.
- Show why a recommendation is being made (for example, "powiązane z Twoją hipotezą zegara neuronalnego").
- Treat inferred preferences as provisional, never as fixed identity.
- Accept "nie wiem", "to zależy", multiple preferences, and contradictions.
- Never infer a diagnosis or claim to know the user's mental state.
- Do not silently turn a temporary state into a permanent profile fact.

## 6. Anti-procrastination design — without productivity theatre

The user's most common bottleneck is getting started, but sustaining, resuming, finishing, and choosing a direction can also be difficult. Anticipatory thoughts about overwhelm, unfinished work, lack of meaning, unreliable information, excessive alternatives, or the possibility that an idea is already known may prevent starting. Sometimes unrelated life thoughts can abruptly flatten motivation.

Translate this into product design:
- Always offer an immediate, low-cost entry point.
- Make "save a fragment and leave" a successful use of the app.
- Offer a **tiny start** action that takes less than a minute and has no obligation to continue.
- Preserve interrupted sessions and make returning to them easy.
- When several directions are possible, show at most a small shortlist and explain the difference; allow "none" and "another direction".
- When the user fears that an idea is already known, offer a gentle prior-art check rather than dismissing it.
- When information feels overwhelming, collapse details behind optional layers and summarise the current state in a few lines.
- Let the user park an idea without losing it.
- Offer one relevant next step, not a large plan, unless the user asks for one.
- Optional reminders must be user-controlled, low frequency, and easy to disable.
- Do not use streaks, guilt, penalties, loss aversion, competitive leaderboards, or productivity scores by default.
- If any gamified element is included, it must be subtle, optional, and reward genuine exploration rather than app-opening frequency. Do not make gamification the core identity.

## 7. Science, faith, philosophy, and epistemic care

The product should be comfortable exploring the relationship between science, philosophy, cosmology, and the user's interest in non-denominational Christianity. It must do so without pushing a religious or anti-religious conclusion.

Separate empirical claims, metaphysical claims, theological interpretations, philosophical arguments, and personal reflections. Explain where scientific methods can test a claim and where a question is outside their direct scope. Represent differing views fairly. Do not use science as decorative proof of theology, or theology as a substitute for empirical evidence.

For speculative physics, quantum mechanics, neuroscience, neurochemistry, and origin-of-universe discussions, correct common misconceptions gently and make the distinction between metaphor and mechanism explicit.

## 8. Safety boundaries

The app is for learning, conceptual modelling, safe creativity, and research literacy. It must not become a procedure generator for risky activities.

- No actionable synthesis, extraction, purification, or handling procedures for dangerous chemicals, toxins, drugs, or hazardous materials.
- No drug dosing, sourcing, combinations, intoxication guidance, or unsupervised pharmacological self-experimentation.
- No medical diagnosis, treatment recommendation, or claims that a supplement/nootropic/biohack is safe or effective for this user.
- No instructions for risky biological engineering, pathogen manipulation, unsafe wet-lab activity, or unsupervised human/animal experimentation.
- No weapon design, construction, optimisation, or other harmful engineering instructions.
- Safe alternatives may include conceptual explanations, historical context, risk awareness, simulations, non-hazardous models, and suggestions to consult qualified professionals or supervised institutions.

## 9. Architecture and implementation preferences

First inspect the existing repository. Preserve any working code and user content. Do not assume the repo is empty. Identify the current stack, routes, build/test commands, deployment configuration, and existing assets before choosing an implementation path.

If the repo is empty or has no established stack, prefer a maintainable, mobile-first PWA with:
- TypeScript and a modern React-based setup (Vite is a reasonable default for a client-first PWA).
- A consistent accessible component system and a restrained design-token layer.
- IndexedDB for personal thoughts, graph data, projects, settings, and local attachments; use a small, typed data-access layer and schema migrations.
- Local-first operation: core capture, browsing, editing, search, projects, reading list, and graph data must remain usable offline.
- A service worker/PWA manifest with clear offline capability and installability where supported.
- A provider-agnostic AI service interface. Do not bind the product to a single model vendor unless the repo already requires it.
- A safe server-side AI gateway or serverless function for provider credentials. Never ship API keys in frontend code, commit them, or store them in localStorage/IndexedDB as if that were secure. If no backend/hosting target or credentials are available, build a truthful adapter/configuration boundary and keep non-AI functionality fully usable; do not fake a live AI response.
- An optional web-research adapter with source metadata and visible online/offline state.
- A graph library only if it materially improves the experience; ensure mobile fallback.
- Export/import of the user's data in a documented JSON format. Validate imports and make a backup before destructive replacement.

Use the repository's established stack if it is already healthy. Do not rewrite the whole app merely to follow the preference above. Avoid adding dependencies without a clear benefit. Keep the code modular, typed, tested, and understandable by a future agent.

### Privacy and persistence

- Local data is the default source of truth.
- No analytics/telemetry by default.
- No automatic upload or sync.
- Before a specific thought or set of notes is sent to an AI service, show what will be sent and give the user an explicit action.
- Clearly distinguish locally available functions from online functions.
- Provide export and deletion controls.
- Never include private note content in logs, error reporting, analytics, URLs, or commit messages.

## 10. Build strategy: deliver value, do not stop at a proposal

Work autonomously within the connected repository, but keep changes reviewable. Do not ask the user to decide minor visual or technical details. Use the brief and make sensible, reversible decisions. Ask only if a missing decision blocks a safe or coherent implementation.

### Phase 0 — inspect

1. Inspect the full repository and git status.
2. Read all relevant project docs, including this file and the two files in `docs/`.
3. Identify the existing stack, application entry point, tests, build scripts, and current functionality.
4. Do not delete, overwrite, or rename existing content without a strong reason.
5. Produce a concise internal implementation plan, then continue into implementation in this task.

### Phase 1 — product and architecture foundation

- Establish navigation and information architecture.
- Define typed entities and persistence schema.
- Define a design system and responsive layout.
- Create useful empty states and sample content only if clearly labelled.

### Phase 2 — implement the useful MVP

Prioritise the following working vertical slice:
1. A beautiful, functional home/lab screen with quick capture and resume-last-thought.
2. Thought Studio with create, edit, autosave, status, tags, search, and related thoughts.
3. A usable project area for long-running ideas.
4. Local persistence that survives refresh/restart.
5. A functional knowledge/relationship view with explicit user-created links (a mobile list view is acceptable as a companion).
6. A useful reading shelf preloaded with the user's named books, editable by the user.
7. Export/import and clear privacy controls.
8. Offline support for core local functions.
9. A real AI integration boundary and honest configuration state; no fake live model output.

Build the core experience end-to-end before creating many shallow modules. If time or tool limits require cuts, prioritise thought capture, return-to-thought, local persistence, and a coherent user experience over decorative features.

### Phase 3 — add the intelligence layer

Once the core works, add the AI modes, curiosity sparks, cross-domain bridges, source checking, and novelty workflow in modular increments. Keep AI suggestions separate from user content and require explicit acceptance before they become permanent graph links or edits.

### Phase 4 — verify

- Run install, type-check, lint, tests, and production build where available.
- Fix errors rather than merely reporting them.
- Test mobile and desktop layouts.
- Verify data persists after reload and export/import works.
- Verify offline behaviour for core functions.
- Verify no secret is included in client output or committed files.
- Inspect key screens in a preview/browser if available.
- Do not claim a test passed unless it was actually run.

## 11. Functional quality bar

No decorative control may pretend to be functional. Every visible button, link, tab, menu, filter, save action, and toggle must work. If a feature is not implemented, either omit it or clearly mark it as unavailable/not configured. No dead buttons, fake graphs, static mock dashboards presented as live data, or simulated AI presented as real AI.

The first release should feel intentionally designed, not like an admin template. It should be visually polished but calm, with clear navigation, readable text, deliberate spacing, a small number of primary actions, useful transitions, and excellent mobile ergonomics.

## 12. Handoff requirements

At the end, report:
1. What was actually implemented.
2. How to run and build it.
3. Which tests/checks were actually executed and their results.
4. Which capabilities need API keys, hosting, or further configuration.
5. What remains incomplete and why.
6. Any important architectural decisions and where they are documented.
7. A preview link or screenshot if the environment supports it.
8. A concise list of changed files.

If GitHub integration supports a working branch and pull request, keep changes on the working branch and open a reviewable PR when appropriate. **Do not merge the PR or change repository permissions without the user's explicit approval.** Never claim that changes were pushed or a PR was created unless the platform confirms it.

## 13. Final product test

Before calling the work complete, ask yourself:

- Can the user capture a fleeting idea in seconds?
- Can they return to an unfinished thought without feeling behind?
- Does the app create curiosity without overwhelming them?
- Does AI support rather than replace their reasoning?
- Can the user tell which words and ideas are theirs versus AI-generated?
- Can a speculative idea coexist with rigorous evidence checking?
- Does finding a known precedent preserve the value of the user's personal rediscovery?
- Does the app work on a phone and retain core functions offline?
- Is personal data private by default?
- Is the app genuinely usable rather than a polished mockup?

If not, improve the product before declaring completion.
