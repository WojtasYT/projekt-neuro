# PROMPT 03 — Give Neurotrino Lab a distinctive, immersive native Android design

Act as a principal product designer and senior Jetpack Compose engineer. The current result is reportedly too minimalist and visually generic. Redesign the native Android experience so that it feels like a **personal scientific laboratory, an explorable universe of ideas, and a creative research console**—not a generic notes app, task manager, or template dashboard.

## Design goal

The user wants a strong visual identity and a sense of discovery. “Minimalist” must not mean empty, flat, visually anonymous, or reduced to a few cards on a dark background. The interface should have character, spatial depth, purposeful information density, and moments of visual wonder while remaining usable and readable.

Use a refined futuristic research-lab aesthetic:
- deep graphite/near-black foundation with subtle tonal layers;
- restrained luminous accents (for example cool cyan, indigo/violet, and a warm amber used sparingly);
- delicate grid/orbital/field-line motifs only where they communicate structure;
- clear typography with editorial contrast between major headings, metadata, and body copy;
- nuanced surfaces, depth, borders, glows, and shadows rather than a stack of identical flat cards;
- custom data visualisation for concepts and relationships;
- subtle, purposeful transitions and micro-interactions.

Do not turn the app into a neon cyberpunk game HUD. Avoid excessive glow, tiny text, decorative noise, fake scientific readouts, random charts, meaningless percentages, and motion that distracts from thinking. Prefer a dark-first theme, but support a polished light theme if feasible. Respect Android accessibility, font scaling, contrast, touch targets, and reduced-motion settings.

## Native Android interaction requirements

- Use Jetpack Compose and native Android navigation/insets/keyboard behaviour.
- No web page inside WebView as the primary UI.
- Design phone layouts first, then adaptive layouts for larger screens.
- Use a bottom navigation bar or another tested phone-native pattern for the main areas; do not hide every destination behind a hamburger menu.
- Keep the quick action “Zapisz myśl” available from the main app shell.
- Provide clear back navigation and preserve unsaved text safely.
- Use meaningful loading, empty, error, selected, pressed, disabled, and offline states.
- Use animations to clarify relationships, navigation, or state changes—not merely to decorate.

## Required screens and visual hierarchy

### 1. Laboratorium (home)

This should feel like entering the user's own research environment. It must not be an empty dashboard or a task list.

Include a distinctive top region and a deliberate hierarchy:
- a personal “what is active in your mind?” area with the last unfinished thought;
- a prominent one-tap “Zapisz myśl” action;
- a single high-quality personalised “Iskra” or curiosity prompt, not an endless feed;
- active projects and recently connected concepts;
- a small “Wróć do wątku” entry point;
- optional context state such as “Mam mgłę”, “Mam ciekawość”, “Mam za dużo wątków” (never treat as diagnosis).

Avoid streak counters, overdue task badges, productivity scores, and daily quotas.

### 2. Szybki zapis

A fast, distraction-light capture surface that opens immediately. One field, autosave, visible save status, optional title/tags later. The user must be able to save a fragment without classifying it or answering a questionnaire.

### 3. Myśl / Pracownia myśli

An immersive but readable editor/detail view. Preserve the raw original text. Show its current development, related thoughts, sources, attachments, and optional structured hypothesis fields progressively. Distinguish user-authored content from AI suggestions visually and semantically.

### 4. Mapa koncepcji

A real, interactive graph view, not a static decorative image. Support pan/zoom, selecting a node, filtering by relation/domain, focusing on one thought and showing neighbours, and opening the thought detail. On small screens, also provide an accessible relationship-list view. The graph must use stored relationships, not fabricated edges.

### 5. Projekty

A visually distinct project space for inventions and longer research efforts. Show the project's purpose, central question, connected thoughts, sources, attachments, and optional next step without turning it into a deadline board.

### 6. Eksploruj / Iskry

A curated exploration space with one or a few meaningful suggestions. Explain why a spark is relevant (e.g. it links to an old thought or a current interest). Allow “Zapisz”, “Inny trop”, “Sprawdź źródła”, and “Pomiń”. No infinite scrolling feed.

### 7. Biblioteka i źródła

A calm editorial reading area for the user's books and saved publications. Treat reading as an invitation, not an obligation. Make it easy to attach a note or question to a book/article.

### 8. Ustawienia i prywatność

Clear settings for data export/import, AI provider, research/search configuration, offline status, notifications (off by default), theme, and data transmission consent.

## Visual design system

Create reusable Compose tokens/components for:
- colour roles and semantic status;
- typography scale;
- spacing and layout grid;
- tonal surface levels;
- cards/panels only where they improve grouping;
- buttons and icon actions;
- chips/tags/relation badges;
- graph nodes/edges;
- sheets/dialogs;
- empty/loading/error states.

Use a small coherent icon language. Avoid emoji as the primary icon system. Avoid inconsistent icon packs and random Unicode symbols.

## Make the UI feel real

Populate screens with realistic sample examples drawn from the user's broad interests—neuroscience, perception, optics, biology, cosmology, engineering concepts, philosophy, and science/faith questions—but label demo records clearly and make them removable. Do not claim that a user's speculative hypothesis is established science. Do not fabricate research citations or pretend a sample note is the user's actual historical note.

If the repository already contains screenshots, design tokens, or assets, inspect them and reuse only what supports the new direction. Do not invent a logo or scientific animation that falsely implies a real measurement.

## Validation

- Capture screenshots for each major screen on a typical Android phone emulator and at least one larger viewport if available.
- Review screenshots for hierarchy, density, contrast, legibility, visual identity, and spacing.
- Test keyboard, system bars, back behaviour, scrolling, and touch targets.
- Confirm every visual control corresponds to a real action. No decorative dead buttons.
- Run build/tests and report actual results.

Do not stop after changing colours. Redesign layout, information hierarchy, navigation, components, and interactions. The result must be recognisably more atmospheric and distinctive than a generic minimalist template while remaining practical for everyday use.
