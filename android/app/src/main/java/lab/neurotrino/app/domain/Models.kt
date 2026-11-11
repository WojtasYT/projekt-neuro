package lab.neurotrino.app.domain

const val EXPORT_FORMAT = "neurotrino-lab"
const val EXPORT_VERSION = 1

enum class ThoughtType { observation, question, hypothesis, model, analogy, invention, reflection, project_note, reading_note, other }
enum class ThoughtStatus { seed, exploring, paused, connected, checking, developed, archived }
enum class Provenance { user, ai, source, joint, sample }
enum class RelationType {
    supports, contradicts, extends, analogy, shared_mechanism, inspired_by,
    source_for, belongs_to_project, metaphor, weak_association
}
enum class ProjectStatus { active, paused, incubating, archived }
enum class ReadingStatus { unopened, exploring, paused, finished }
enum class MindState { fog, curiosity, lowEnergy, tooMany, impulse }
enum class AiMode { iskra, sokrates, lacznik, adwokat, weryfikator, rozwiniecie, powrot }
enum class GraphKind { thought, project, reading, source, concept }

data class HypothesisFields(
    val observation: String = "",
    val proposal: String = "",
    val mechanism: String = "",
    val assumptions: String = "",
    val known: String = "",
    val alternatives: String = "",
    val expectations: String = "",
    val falsifiers: String = "",
    val nextStep: String = "",
    val related: String = "",
)

data class Expansion(
    val id: String,
    val text: String,
    val provenance: Provenance,
    val createdAt: String,
)

data class Thought(
    val id: String,
    val title: String,
    val rawText: String,
    val content: String,
    val types: List<ThoughtType>,
    val status: ThoughtStatus,
    val tags: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val lastOpenedAt: String,
    val provenance: Provenance,
    val projectIds: List<String>,
    val sourceIds: List<String>,
    val readingItemIds: List<String>,
    val isSample: Boolean = false,
    val hypothesis: HypothesisFields? = null,
    val expansions: List<Expansion> = emptyList(),
)

data class Project(
    val id: String,
    val title: String,
    val purpose: String,
    val description: String,
    val status: ProjectStatus,
    val nextStep: String,
    val createdAt: String,
    val updatedAt: String,
    val isSample: Boolean = false,
    val history: List<HistoryNote> = emptyList(),
)

data class HistoryNote(val at: String, val note: String)

data class Relationship(
    val id: String,
    val fromId: String,
    val toId: String,
    val fromKind: GraphKind,
    val toKind: GraphKind,
    val relationType: RelationType,
    val explanation: String,
    val provenance: Provenance,
    val confidence: String? = null,
    val createdAt: String,
)

data class ReadingItem(
    val id: String,
    val title: String,
    val author: String,
    val status: ReadingStatus,
    val progressNote: String,
    val currentPage: String,
    val notes: String,
    val isStarter: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
)

data class SourceItem(
    val id: String,
    val title: String,
    val authors: String,
    val year: String = "",
    val publisher: String = "",
    val doi: String = "",
    val url: String = "",
    val sourceType: String = "other",
    val notes: String = "",
    val accessedAt: String = "",
    val createdAt: String,
)

data class Spark(
    val id: String,
    val title: String,
    val hook: String,
    val explanation: String,
    val deeper: String = "",
    val question: String,
    val domains: List<String>,
    val sourceNote: String = "",
    val kind: String,
    val generatedBy: String,
    val status: String = "fresh",
    val linkedThoughtIds: List<String> = emptyList(),
    val why: String = "",
)

data class AttachmentMeta(
    val id: String,
    val thoughtId: String,
    val name: String,
    val mime: String,
    val uri: String,
    val createdAt: String,
    val size: Long,
)

data class UserProfile(
    val displayName: String = "",
    val interests: List<String> = emptyList(),
    val currentFocus: String = "",
    val onboarded: Boolean = false,
    val theme: String = "dark",
    val aiDefaultMode: AiMode = AiMode.iskra,
    val gatewayUrl: String = "",
    val model: String = "",
    val remindersEnabled: Boolean = false,
    val lastMindState: MindState? = null,
    val notesToSelf: String = "",
    val updatedAt: String = "",
)

fun previewTitle(text: String): String {
    val line = text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return ""
    return if (line.length > 72) line.take(69) + "…" else line
}

fun splitFragments(text: String): List<String> =
    text.split(Regex("\n\\s*\n")).map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty {
        listOf(text.trim()).filter { it.isNotEmpty() }
    }

fun normalizeQuery(value: String): String =
    java.text.Normalizer.normalize(value.lowercase(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .trim()

fun matchesQuery(haystack: String, query: String): Boolean {
    if (query.isBlank()) return true
    return normalizeQuery(haystack).contains(normalizeQuery(query))
}

fun thoughtHaystack(thought: Thought): String = buildString {
    append(thought.title); append(' ')
    append(thought.rawText); append(' ')
    append(thought.content); append(' ')
    append(thought.tags.joinToString(" "))
    thought.hypothesis?.let {
        append(' ')
        append(listOf(it.observation, it.proposal, it.mechanism, it.assumptions, it.known, it.alternatives, it.expectations, it.falsifiers, it.nextStep, it.related).joinToString(" "))
    }
    thought.expansions.forEach { append(' '); append(it.text) }
}

fun searchThoughts(thoughts: List<Thought>, query: String): List<Thought> =
    thoughts.filter { matchesQuery(thoughtHaystack(it), query) }

fun lastResumable(thoughts: List<Thought>): Thought? =
    thoughts.filter { it.status != ThoughtStatus.archived }.maxByOrNull { it.lastOpenedAt }
