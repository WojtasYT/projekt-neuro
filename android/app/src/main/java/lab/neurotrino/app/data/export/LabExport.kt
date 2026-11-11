package lab.neurotrino.app.data.export

import lab.neurotrino.app.data.db.ExpansionDto
import lab.neurotrino.app.data.db.HistoryDto
import lab.neurotrino.app.data.db.HypothesisDto
import lab.neurotrino.app.data.db.jsonSilent
import lab.neurotrino.app.domain.EXPORT_FORMAT
import lab.neurotrino.app.domain.EXPORT_VERSION
import lab.neurotrino.app.domain.AiMode
import lab.neurotrino.app.domain.Expansion
import lab.neurotrino.app.domain.GraphKind
import lab.neurotrino.app.domain.HistoryNote
import lab.neurotrino.app.domain.HypothesisFields
import lab.neurotrino.app.domain.MindState
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.ReadingStatus
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.Relationship
import lab.neurotrino.app.domain.SourceItem
import lab.neurotrino.app.domain.Spark
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.domain.UserProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LabExportFile(
    val format: String,
    val version: Int,
    val exportedAt: String,
    val thoughts: List<ThoughtDto> = emptyList(),
    val projects: List<ProjectDto> = emptyList(),
    val relationships: List<RelationshipDto> = emptyList(),
    val sources: List<SourceDto> = emptyList(),
    val readingItems: List<ReadingDto> = emptyList(),
    val sparks: List<SparkDto> = emptyList(),
    val profile: ProfileDto,
)

@Serializable
data class ThoughtDto(
    val id: String,
    val title: String,
    val rawText: String,
    val content: String,
    val types: List<String>,
    val status: String,
    val tags: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val lastOpenedAt: String,
    val provenance: String,
    val projectIds: List<String> = emptyList(),
    val sourceIds: List<String> = emptyList(),
    val readingItemIds: List<String> = emptyList(),
    val isSample: Boolean? = null,
    val hypothesis: HypothesisDto? = null,
    val expansions: List<ExpansionDto> = emptyList(),
)

@Serializable
data class ProjectDto(
    val id: String,
    val title: String,
    val purpose: String,
    val description: String,
    val status: String,
    val nextStep: String,
    val createdAt: String,
    val updatedAt: String,
    val isSample: Boolean? = null,
    val history: List<HistoryDto> = emptyList(),
)

@Serializable
data class RelationshipDto(
    val id: String,
    val fromId: String,
    val toId: String,
    val fromKind: String,
    val toKind: String,
    val relationType: String,
    val explanation: String,
    val provenance: String,
    val confidence: String? = null,
    val createdAt: String,
)

@Serializable
data class SourceDto(
    val id: String,
    val title: String,
    val authors: String,
    val year: String? = null,
    val publisher: String? = null,
    val doi: String? = null,
    val url: String? = null,
    val sourceType: String,
    val notes: String,
    val accessedAt: String? = null,
    val createdAt: String,
)

@Serializable
data class ReadingDto(
    val id: String,
    val title: String,
    val author: String,
    val status: String,
    val progressNote: String,
    val currentPage: String,
    val notes: String,
    val isStarter: Boolean? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SparkDto(
    val id: String,
    val title: String,
    val hook: String,
    val explanation: String,
    val deeper: String? = null,
    val question: String,
    val domains: List<String>,
    val sourceNote: String? = null,
    val kind: String,
    val generatedBy: String,
    val status: String,
    val linkedThoughtIds: List<String> = emptyList(),
)

@Serializable
data class ProfileDto(
    val id: String = "local",
    val displayName: String,
    val interests: List<String>,
    val currentFocus: String,
    val onboarded: Boolean,
    val theme: String,
    val aiDefaultMode: String,
    val aiConfig: AiConfigDto = AiConfigDto(),
    val remindersEnabled: Boolean,
    val lastMindState: String? = null,
    val notesToSelf: String,
    val updatedAt: String,
)

@Serializable
data class AiConfigDto(val gatewayUrl: String = "", val model: String? = null)

fun Thought.toDto() = ThoughtDto(
    id, title, rawText, content, types.map { it.name }, status.name, tags,
    createdAt, updatedAt, lastOpenedAt, provenance.name, projectIds, sourceIds, readingItemIds, isSample,
    hypothesis?.let {
        HypothesisDto(it.observation, it.proposal, it.mechanism, it.assumptions, it.known, it.alternatives, it.expectations, it.falsifiers, it.nextStep, it.related)
    },
    expansions.map { ExpansionDto(it.id, it.text, it.provenance.name, it.createdAt) },
)

fun ThoughtDto.toDomain() = Thought(
    id, title, rawText, content,
    types.mapNotNull { runCatching { ThoughtType.valueOf(it) }.getOrNull() },
    runCatching { ThoughtStatus.valueOf(status) }.getOrDefault(ThoughtStatus.seed),
    tags, createdAt, updatedAt, lastOpenedAt,
    runCatching { Provenance.valueOf(provenance) }.getOrDefault(Provenance.user),
    projectIds, sourceIds, readingItemIds, isSample == true,
    hypothesis?.let {
        HypothesisFields(it.observation.orEmpty(), it.proposal.orEmpty(), it.mechanism.orEmpty(), it.assumptions.orEmpty(), it.known.orEmpty(), it.alternatives.orEmpty(), it.expectations.orEmpty(), it.falsifiers.orEmpty(), it.nextStep.orEmpty(), it.related.orEmpty())
    },
    expansions.map { Expansion(it.id, it.text, runCatching { Provenance.valueOf(it.provenance) }.getOrDefault(Provenance.user), it.createdAt) },
)

fun Project.toDto() = ProjectDto(id, title, purpose, description, status.name, nextStep, createdAt, updatedAt, isSample, history.map { HistoryDto(it.at, it.note) })
fun ProjectDto.toDomain() = Project(
    id, title, purpose, description,
    runCatching { ProjectStatus.valueOf(status) }.getOrDefault(ProjectStatus.active),
    nextStep, createdAt, updatedAt, isSample == true,
    history.map { HistoryNote(it.at, it.note) },
)

fun Relationship.toDto() = RelationshipDto(id, fromId, toId, fromKind.name, toKind.name, relationType.name, explanation, provenance.name, confidence, createdAt)
fun RelationshipDto.toDomain() = Relationship(
    id, fromId, toId,
    runCatching { GraphKind.valueOf(fromKind) }.getOrDefault(GraphKind.thought),
    runCatching { GraphKind.valueOf(toKind) }.getOrDefault(GraphKind.thought),
    runCatching { RelationType.valueOf(relationType) }.getOrDefault(RelationType.extends),
    explanation, runCatching { Provenance.valueOf(provenance) }.getOrDefault(Provenance.user), confidence, createdAt,
)

fun ReadingItem.toDto() = ReadingDto(id, title, author, status.name, progressNote, currentPage, notes, isStarter, createdAt, updatedAt)
fun ReadingDto.toDomain() = ReadingItem(
    id, title, author,
    runCatching { ReadingStatus.valueOf(status) }.getOrDefault(ReadingStatus.unopened),
    progressNote, currentPage, notes, isStarter == true, createdAt, updatedAt,
)

fun SourceItem.toDto() = SourceDto(id, title, authors, year, publisher, doi, url, sourceType, notes, accessedAt, createdAt)
fun SourceDto.toDomain() = SourceItem(id, title, authors, year.orEmpty(), publisher.orEmpty(), doi.orEmpty(), url.orEmpty(), sourceType, notes, accessedAt.orEmpty(), createdAt)

fun UserProfile.toDto() = ProfileDto(
    displayName = displayName, interests = interests, currentFocus = currentFocus, onboarded = onboarded,
    theme = theme, aiDefaultMode = aiDefaultMode.name, aiConfig = AiConfigDto(gatewayUrl, model.ifBlank { null }),
    remindersEnabled = remindersEnabled,
    lastMindState = lastMindState?.let { mindToPwa(it) },
    notesToSelf = notesToSelf, updatedAt = updatedAt,
)

fun ProfileDto.toDomain() = UserProfile(
    displayName, interests, currentFocus, onboarded, theme,
    runCatching { AiMode.valueOf(aiDefaultMode) }.getOrDefault(AiMode.iskra),
    aiConfig.gatewayUrl, aiConfig.model.orEmpty(), remindersEnabled,
    lastMindState?.let { mindFromPwa(it) }, notesToSelf, updatedAt,
)

fun mindToPwa(state: MindState) = when (state) {
    MindState.fog -> "mgła"
    MindState.curiosity -> "ciekawość"
    MindState.lowEnergy -> "mało_energii"
    MindState.tooMany -> "za_dużo_wątków"
    MindState.impulse -> "impuls"
}

fun mindFromPwa(raw: String) = when (raw) {
    "mgła" -> MindState.fog
    "ciekawość" -> MindState.curiosity
    "mało_energii" -> MindState.lowEnergy
    "za_dużo_wątków" -> MindState.tooMany
    "impuls" -> MindState.impulse
    else -> runCatching { MindState.valueOf(raw) }.getOrNull()
}

fun parseExport(raw: String): LabExportFile {
    val parsed = runCatching { jsonSilent.decodeFromString<LabExportFile>(raw) }.getOrElse {
        throw IllegalArgumentException("Plik nie wygląda na zapis Neurotrino Lab.")
    }
    if (parsed.format != EXPORT_FORMAT) {
        throw IllegalArgumentException("To nie jest plik formatu neurotrino-lab (jest: ${parsed.format}).")
    }
    if (parsed.version != EXPORT_VERSION) {
        throw IllegalArgumentException("Nieobsługiwana wersja zapisu (${parsed.version}). Oczekiwano $EXPORT_VERSION.")
    }
    parsed.thoughts.forEach {
        if (it.id.isBlank()) throw IllegalArgumentException("Myśl bez identyfikatora.")
    }
    return parsed
}

fun emptyExport(now: String, profile: UserProfile) = LabExportFile(
    format = EXPORT_FORMAT,
    version = EXPORT_VERSION,
    exportedAt = now,
    profile = profile.toDto(),
)
