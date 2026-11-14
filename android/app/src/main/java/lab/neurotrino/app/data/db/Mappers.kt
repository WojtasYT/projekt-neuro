package lab.neurotrino.app.data.db

import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.AssistantMessage
import lab.neurotrino.app.domain.Conversation
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
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.domain.UserProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class HypothesisDto(
    val observation: String? = null,
    val proposal: String? = null,
    val mechanism: String? = null,
    val assumptions: String? = null,
    val known: String? = null,
    val alternatives: String? = null,
    val expectations: String? = null,
    val falsifiers: String? = null,
    val nextStep: String? = null,
    val related: String? = null,
)

@Serializable
data class ExpansionDto(
    val id: String,
    val text: String,
    val provenance: String,
    val createdAt: String,
)

@Serializable
data class HistoryDto(val at: String, val note: String)

fun Thought.toRow(): ThoughtRow = ThoughtRow(
    id = id,
    title = title,
    rawText = rawText,
    content = content,
    typesJson = encodeList(types.map { it.name }),
    status = status.name,
    tagsJson = encodeList(tags),
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastOpenedAt = lastOpenedAt,
    provenance = provenance.name,
    projectIdsJson = encodeList(projectIds),
    sourceIdsJson = encodeList(sourceIds),
    readingItemIdsJson = encodeList(readingItemIds),
    isSample = isSample,
    hypothesisJson = hypothesis?.let {
        jsonSilent.encodeToString(
            HypothesisDto(
                it.observation, it.proposal, it.mechanism, it.assumptions, it.known,
                it.alternatives, it.expectations, it.falsifiers, it.nextStep, it.related,
            ),
        )
    } ?: "",
    expansionsJson = jsonSilent.encodeToString(expansions.map { ExpansionDto(it.id, it.text, it.provenance.name, it.createdAt) }),
)

fun ThoughtRow.toDomain(): Thought = Thought(
    id = id,
    title = title,
    rawText = rawText,
    content = content,
    types = decodeList(typesJson).mapNotNull { runCatching { ThoughtType.valueOf(it) }.getOrNull() },
    status = runCatching { ThoughtStatus.valueOf(status) }.getOrDefault(ThoughtStatus.seed),
    tags = decodeList(tagsJson),
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastOpenedAt = lastOpenedAt,
    provenance = runCatching { Provenance.valueOf(provenance) }.getOrDefault(Provenance.user),
    projectIds = decodeList(projectIdsJson),
    sourceIds = decodeList(sourceIdsJson),
    readingItemIds = decodeList(readingItemIdsJson),
    isSample = isSample,
    hypothesis = hypothesisJson.takeIf { it.isNotBlank() }?.let { raw ->
        jsonSilent.decodeFromString<HypothesisDto>(raw).let {
            HypothesisFields(
                it.observation.orEmpty(), it.proposal.orEmpty(), it.mechanism.orEmpty(),
                it.assumptions.orEmpty(), it.known.orEmpty(), it.alternatives.orEmpty(),
                it.expectations.orEmpty(), it.falsifiers.orEmpty(), it.nextStep.orEmpty(), it.related.orEmpty(),
            )
        }
    },
    expansions = if (expansionsJson.isBlank()) emptyList() else jsonSilent.decodeFromString<List<ExpansionDto>>(expansionsJson)
        .map { Expansion(it.id, it.text, runCatching { Provenance.valueOf(it.provenance) }.getOrDefault(Provenance.user), it.createdAt) },
)

fun Project.toRow() = ProjectRow(
    id, title, purpose, description, status.name, nextStep, createdAt, updatedAt, isSample,
    jsonSilent.encodeToString(history.map { HistoryDto(it.at, it.note) }),
)

fun ProjectRow.toDomain() = Project(
    id, title, purpose, description,
    runCatching { ProjectStatus.valueOf(status) }.getOrDefault(ProjectStatus.active),
    nextStep, createdAt, updatedAt, isSample,
    if (historyJson.isBlank()) emptyList() else jsonSilent.decodeFromString<List<HistoryDto>>(historyJson).map { HistoryNote(it.at, it.note) },
)

fun Relationship.toRow() = RelationshipRow(
    id, fromId, toId, fromKind.name, toKind.name, relationType.name, explanation, provenance.name, confidence, createdAt,
)

fun RelationshipRow.toDomain() = Relationship(
    id, fromId, toId,
    runCatching { GraphKind.valueOf(fromKind) }.getOrDefault(GraphKind.thought),
    runCatching { GraphKind.valueOf(toKind) }.getOrDefault(GraphKind.thought),
    runCatching { RelationType.valueOf(relationType) }.getOrDefault(RelationType.extends),
    explanation,
    runCatching { Provenance.valueOf(provenance) }.getOrDefault(Provenance.user),
    confidence, createdAt,
)

fun ReadingItem.toRow() = ReadingRow(id, title, author, status.name, progressNote, currentPage, notes, isStarter, createdAt, updatedAt)

fun ReadingRow.toDomain() = ReadingItem(
    id, title, author,
    runCatching { ReadingStatus.valueOf(status) }.getOrDefault(ReadingStatus.unopened),
    progressNote, currentPage, notes, isStarter, createdAt, updatedAt,
)

fun UserProfile.toRow() = ProfileRow(
    displayName = displayName,
    interestsJson = encodeList(interests),
    currentFocus = currentFocus,
    onboarded = onboarded,
    theme = theme,
    aiDefaultMode = aiDefaultMode.name,
    gatewayUrl = gatewayUrl,
    model = model,
    remindersEnabled = remindersEnabled,
    lastMindState = lastMindState?.name,
    notesToSelf = notesToSelf,
    updatedAt = updatedAt,
)

fun ProfileRow.toDomain() = UserProfile(
    displayName, decodeList(interestsJson), currentFocus, onboarded, theme,
    runCatching { AssistantMode.valueOf(aiDefaultMode) }.getOrDefault(AssistantMode.impuls),
    gatewayUrl, model, remindersEnabled,
    lastMindState?.let { runCatching { MindState.valueOf(it) }.getOrNull() },
    notesToSelf, updatedAt,
)

fun lab.neurotrino.app.domain.SourceItem.toRow() = SourceRow(
    id, title, authors, year, publisher, doi, url, sourceType, notes, accessedAt, createdAt,
)

fun SourceRow.toDomain() = lab.neurotrino.app.domain.SourceItem(
    id, title, authors, year, publisher, doi, url, sourceType, notes, accessedAt, createdAt,
)

fun lab.neurotrino.app.domain.AttachmentMeta.toRow() = AttachmentRow(
    id, thoughtId, name, mime, uri, createdAt, size,
)

fun Conversation.toRow() = ConversationRow(
    id = id, title = title, projectId = projectId, sourceThoughtId = sourceThoughtId,
    mode = mode.name, archived = archived, createdAt = createdAt, updatedAt = updatedAt,
)

fun ConversationRow.toDomain() = Conversation(
    id = id, title = title, projectId = projectId, sourceThoughtId = sourceThoughtId,
    mode = runCatching { AssistantMode.valueOf(mode) }.getOrDefault(AssistantMode.impuls),
    archived = archived, createdAt = createdAt, updatedAt = updatedAt,
)

fun AssistantMessage.toRow() = MessageRow(
    id = id, conversationId = conversationId, role = role, content = content,
    model = model, provenance = provenance.name, createdAt = createdAt,
)

fun MessageRow.toDomain() = AssistantMessage(
    id = id, conversationId = conversationId, role = role, content = content,
    model = model,
    provenance = runCatching { Provenance.valueOf(provenance) }.getOrDefault(Provenance.user),
    createdAt = createdAt,
)
