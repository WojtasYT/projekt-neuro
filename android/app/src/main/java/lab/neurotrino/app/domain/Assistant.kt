package lab.neurotrino.app.domain

/** Modele domenowe Asystenta (V4) + czysta logika kontekstu (testowalna jednostkowo). */

data class Conversation(
    val id: String,
    val title: String,
    val projectId: String? = null,
    val sourceThoughtId: String? = null,
    val mode: AssistantMode = AssistantMode.impuls,
    val archived: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
)

data class AssistantMessage(
    val id: String,
    val conversationId: String,
    val role: String, // "user" | "assistant"
    val content: String,
    val model: String? = null,
    val provenance: Provenance, // user dla wpisów użytkownika, ai dla odpowiedzi modelu
    val createdAt: String,
)

data class AssistantContextItem(
    /** project | source | thought | concept */
    val kind: String,
    val id: String,
    val title: String,
    val text: String,
)

/** Pierwsza sensowna linia użytkownika jako tytuł rozmowy. */
fun conversationTitle(seed: String, limit: Int = 64): String {
    val line = seed.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return "Nowa rozmowa"
    return if (line.length > limit) line.take(limit - 1) + "…" else line
}

/**
 * Zbuduj listę elementów kontekstu wysyłanych do bramki.
 * Priorytet i limity: 1 projekt, do 3 źródeł, do 4 powiązanych myśli.
 * Deduplikacja po id i po znormalizowanym tekście. Wykluczenia (UI) stosuje wywołujący.
 */
fun buildContextItems(
    thought: Thought?,
    linkedThoughts: List<Thought>,
    projects: List<Project>,
    sources: List<SourceItem>,
    excluded: Set<String> = emptySet(),
): List<AssistantContextItem> {
    val out = mutableListOf<AssistantContextItem>()
    val seenText = mutableSetOf<String>()
    if (thought != null) {
        projects.firstOrNull { it.id == thought.projectIds.firstOrNull() }?.let { p ->
            out += AssistantContextItem(
                kind = "project",
                id = "project:${p.id}",
                title = p.title,
                text = listOf(p.purpose, p.description, p.nextStep.ifBlank { null }?.let { "Następny krok: $it" })
                    .filterNotNull().joinToString("\n").trim(),
            )
        }
        sources.filter { it.id in thought.sourceIds }.take(3).forEach { s ->
            out += AssistantContextItem(
                kind = "source",
                id = "source:${s.id}",
                title = s.title,
                text = listOf(s.authors, s.year, s.doi, s.url, s.notes).filter { it.isNotBlank() }.joinToString(" · "),
            )
        }
    }
    linkedThoughts.take(4).forEach { t ->
        val body = t.content.ifBlank { t.rawText }.trim()
        if (body.isBlank()) return@forEach
        val norm = body.lowercase().replace(Regex("\\s+"), " ").take(200)
        if (!seenText.add(norm)) return@forEach
        out += AssistantContextItem(
            kind = "thought",
            id = "thought:${t.id}",
            title = t.title.ifBlank { previewTitle(body) },
            text = body.take(1200),
        )
    }
    return out.filterNot { it.id in excluded }
}

/** Zadanie walidacji referencji komunikat→rozmowa przy imporcie (czysta logika, testowalna). */
fun missingConversationRefs(
    messageConversationIds: List<String>,
    knownConversationIds: Set<String>,
): List<String> = messageConversationIds.filter { it !in knownConversationIds }.distinct()

/** Normalizacja konfiguracji bramki wpisanej przez użytkownika → baza do której doklejane są ścieżki /v1/*. */
fun normalizeGatewayBase(url: String): String {
    var base = url.trim().trimEnd('/')
    val suffixes = listOf(
        "/v1/chat/completions", "/v1/context/preview", "/v1/models", "/v1/ai/status", "/health", "/v1",
    )
    for (s in suffixes) {
        if (base.endsWith(s, ignoreCase = true)) base = base.dropLast(s.length).trimEnd('/')
    }
    return base
}

/** Podgląd treści wysyłanej do modelu (jawna zgoda) — bez sekretów, bez skrótów myślowych. */
fun thoughtDispatchPreview(thought: Thought, limit: Int = 1200): String {
    val body = thought.content.ifBlank { thought.rawText }.trim()
    val clipped = if (body.length > limit) body.take(limit) + "…" else body
    return listOf(thought.title, clipped).filter { it.isNotBlank() }.joinToString("\n\n")
}
