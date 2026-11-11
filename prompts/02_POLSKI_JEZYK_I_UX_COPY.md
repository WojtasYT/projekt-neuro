# PROMPT 02 — Rewrite all user-facing Polish copy to fluent, precise Polish

Act as a senior Polish UX writer and editor with experience in scientific software. Audit and rewrite **every user-visible string** in the current application, especially the native Android version. This is a language-quality task, not a superficial spellcheck.

## The problem to fix

The current interface is technically in Polish but often sounds translated, unnatural, vague, or artificially technical. Some labels appear to name a concept without telling the user what the feature actually does. The user wants precise, fluent Polish that sounds as if it was originally written in Polish by an intelligent human—not machine translation and not pseudo-academic jargon.

## Language rules

1. Write natural contemporary Polish with correct cases, aspect, word order, punctuation, and idiomatic verbs.
2. Prefer concrete action labels. A button should say what happens when pressed.
3. Be specific without becoming verbose. A short sentence is good only if it remains meaningful.
4. Avoid calques from English, awkward nominalisations, filler words, invented jargon, and abstract words such as “proces”, “przestrzeń”, “eksploracja”, “aktywacja” or “optymalizacja” when a concrete verb is clearer.
5. Do not force every feature into a grandiose “scientific” vocabulary. Use scientific terminology only when it is accurate and useful.
6. Avoid school/teacher language, generic motivational slogans, patronising praise, and productivity clichés.
7. Do not overuse English. Keep established proper names and technical terms only where Polish users genuinely use them. Explain a necessary specialist term in context.
8. Never translate the product name Neurotrino Lab or names of internal AI modes mechanically. Choose clear Polish labels for visible navigation.
9. Do not use ambiguous labels like “Uruchom”, “Rozwiń”, “Zarządzaj”, “Kontynuuj” without context where a more precise label is possible.
10. Preserve scientific nuance and uncertainty. Do not make speculative statements sound established.

## Suggested terminology (adapt where the current UI gives a better, clearer name)

- “Laboratorium” — the home/research overview.
- “Zapisz myśl” — capture a raw thought.
- “Wróć do wątku” — resume an unfinished thought.
- “Pracownia myśli” — edit and develop one thought (use only if it reads naturally in context).
- “Iskra” — one short curiosity prompt; explain the content rather than relying on the metaphor alone.
- “Połączenia” — relationships between saved thoughts/concepts.
- “Mapa koncepcji” — graph/map view.
- “Projekty” — longer-running invention or research containers.
- “Źródła” — papers, articles, books, and references.
- “Biblioteka” — user's books and reading notes.
- “Sprawdź, co już wiadomo” — user-triggered evidence/novelty search.
- “Dodaj powiązanie” — create a relationship between two ideas.
- “Zapisano” / “Nie udało się zapisać” — clear save feedback.

Do not blindly apply this glossary if a label becomes unnatural in a particular sentence.

## Example of the required quality

Bad: “Aktywuj swój potencjał poznawczy poprzez eksplorację idei.”
Good: “Zapisz to, co właśnie przyszło Ci do głowy.”

Bad: “Twoje procesy intelektualne czekają na kontynuację.”
Good: “Ostatnio rozważałeś, czy dwa zapisane mechanizmy mają ze sobą coś wspólnego.”

Bad: “Inicjuj nowy kierunek badawczy.”
Good: “Zacznij od pytania, które Cię teraz ciekawi.”

Bad: “Wystąpił błąd podczas persystencji danych.”
Good: “Nie udało się zapisać zmian. Twoja poprzednia wersja nadal jest dostępna.”

These are examples of tone, not literal strings to insert everywhere.

## Required workflow

1. Find all UI strings, including navigation, buttons, field labels, placeholders, onboarding, tooltips, empty states, error/success messages, dialogs, settings, notification text, AI mode labels, sample content, and accessibility descriptions.
2. Create a string inventory. Rewrite each string in context; do not perform blind word replacement.
3. Keep copy in a central resource/localisation layer rather than scattered throughout composables/components.
4. Check that button labels match actual implemented actions.
5. Ensure no placeholder copy, Lorem Ipsum, machine-translated fragments, English debugging text, or unexplained internal labels leak into the UI.
6. Add a copy QA checklist/test or a reviewable string table containing old string, revised string, screen/context, and rationale.
7. Check grammatical consistency: addressing the user in second person singular where appropriate; avoid switching between formal “Pan” and informal “Ty”. Prefer neutral phrasing when direct address is unnecessary.
8. Review text at real phone width. Avoid clipped labels and awkward multi-line wrapping.

## Acceptance criteria

- A native Polish speaker would not describe the interface as “Polish translated from English”.
- Every feature label tells the user what it does or is immediately clear from context.
- All errors explain what happened and what the user can do next.
- Empty states offer a useful next action without guilt or pressure.
- Scientific labels remain accurate and do not inflate speculation into fact.
- All user-facing copy is Polish, except intentional product names or genuinely established technical terms.

Implement the changes, run tests/build, and report the files changed plus a representative before/after table. Do not change the core meaning of features merely to make the text sound more impressive.
