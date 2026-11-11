package lab.neurotrino.app.data.repo

import android.content.Context
import android.net.Uri
import lab.neurotrino.app.data.db.AttachmentRow
import lab.neurotrino.app.data.db.MetaRow
import lab.neurotrino.app.data.db.NeurotrinoDatabase
import lab.neurotrino.app.data.db.defaultProfile
import lab.neurotrino.app.data.db.ensureSeeded
import lab.neurotrino.app.data.db.jsonPretty
import lab.neurotrino.app.data.db.starterLibrary
import lab.neurotrino.app.data.db.toDomain
import lab.neurotrino.app.data.db.toRow
import lab.neurotrino.app.data.export.LabExportFile
import lab.neurotrino.app.data.export.parseExport
import lab.neurotrino.app.data.export.toDomain
import lab.neurotrino.app.data.export.toDto
import lab.neurotrino.app.domain.AttachmentMeta
import lab.neurotrino.app.domain.EXPORT_FORMAT
import lab.neurotrino.app.domain.EXPORT_VERSION
import lab.neurotrino.app.domain.Expansion
import lab.neurotrino.app.domain.GraphKind
import lab.neurotrino.app.domain.HistoryNote
import lab.neurotrino.app.domain.HypothesisFields
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
import lab.neurotrino.app.domain.previewTitle
import lab.neurotrino.app.domain.splitFragments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import java.time.Instant
import java.util.UUID

class LabRepository(private val db: NeurotrinoDatabase) {

    val thoughts: Flow<List<Thought>> = db.thoughts().observe().map { rows -> rows.map { it.toDomain() } }
    val projects: Flow<List<Project>> = db.projects().observe().map { rows -> rows.map { it.toDomain() } }
    val relationships: Flow<List<Relationship>> = db.relationships().observe().map { rows -> rows.map { it.toDomain() } }
    val reading: Flow<List<ReadingItem>> = db.reading().observe().map { rows -> rows.map { it.toDomain() } }
    val profile: Flow<UserProfile> = db.profile().observe().map { it?.toDomain() ?: defaultProfile() }

    fun thought(id: String): Flow<Thought?> = db.thoughts().observeOne(id).map { it?.toDomain() }
    fun project(id: String): Flow<Project?> = db.projects().observeOne(id).map { it?.toDomain() }
    fun book(id: String): Flow<ReadingItem?> = db.reading().observeOne(id).map { it?.toDomain() }
    fun attachments(thoughtId: String): Flow<List<AttachmentMeta>> =
        db.attachments().observeForThought(thoughtId).map { rows -> rows.map { it.toMeta() } }

    suspend fun start() = db.ensureSeeded()

    suspend fun capture(text: String, readingId: String? = null, projectId: String? = null): List<Thought> {
        val pieces = splitFragments(text)
        return pieces.map { piece ->
            val stamp = now()
            val thought = Thought(
                id = id("th"),
                title = previewTitle(piece),
                rawText = piece,
                content = piece,
                types = if (readingId != null) listOf(ThoughtType.reading_note) else emptyList(),
                status = ThoughtStatus.seed,
                tags = emptyList(),
                createdAt = stamp,
                updatedAt = stamp,
                lastOpenedAt = stamp,
                provenance = Provenance.user,
                projectIds = listOfNotNull(projectId),
                sourceIds = emptyList(),
                readingItemIds = listOfNotNull(readingId),
            )
            db.thoughts().upsert(thought.toRow())
            thought
        }
    }

    suspend fun updateThought(thought: Thought) {
        val existing = db.thoughts().get(thought.id) ?: return
        val merged = thought.copy(
            rawText = existing.rawText,
            updatedAt = now(),
            title = thought.title.ifBlank { previewTitle(thought.content.ifBlank { existing.rawText }) },
        )
        db.thoughts().upsert(merged.toRow())
    }

    suspend fun touch(id: String) {
        val row = db.thoughts().get(id) ?: return
        db.thoughts().upsert(row.copy(lastOpenedAt = now()))
    }

    suspend fun setStatus(id: String, status: ThoughtStatus) {
        val t = db.thoughts().get(id)?.toDomain() ?: return
        updateThought(t.copy(status = status))
    }

    suspend fun addExpansion(id: String, text: String, provenance: Provenance) {
        val t = db.thoughts().get(id)?.toDomain() ?: return
        updateThought(
            t.copy(
                expansions = t.expansions + Expansion(id("ex"), text.trim(), provenance, now()),
            ),
        )
    }

    suspend fun setHypothesis(id: String, fields: HypothesisFields) {
        val t = db.thoughts().get(id)?.toDomain() ?: return
        updateThought(t.copy(hypothesis = fields))
    }

    suspend fun deleteThought(id: String) {
        db.thoughts().delete(id)
        db.relationships().deleteTouching(id)
        db.attachments().deleteForThought(id)
    }

    suspend fun createProject(title: String, purpose: String): Project {
        val stamp = now()
        val project = Project(
            id = id("pr"),
            title = title.ifBlank { "Bez nazwy" },
            purpose = purpose,
            description = "",
            status = ProjectStatus.active,
            nextStep = "",
            createdAt = stamp,
            updatedAt = stamp,
            history = listOf(HistoryNote(stamp, "Utworzono projekt.")),
        )
        db.projects().upsert(project.toRow())
        return project
    }

    suspend fun updateProject(project: Project, historyNote: String? = null) {
        val stamp = now()
        val next = project.copy(
            updatedAt = stamp,
            history = if (historyNote != null) project.history + HistoryNote(stamp, historyNote) else project.history,
        )
        db.projects().upsert(next.toRow())
    }

    suspend fun deleteProject(id: String) {
        db.projects().delete(id)
        db.thoughts().all().forEach { row ->
            val t = row.toDomain()
            if (id in t.projectIds) {
                db.thoughts().upsert(t.copy(projectIds = t.projectIds - id, updatedAt = now()).toRow())
            }
        }
        db.relationships().deleteTouching(id)
    }

    suspend fun attachThoughtToProject(thoughtId: String, projectId: String) {
        val t = db.thoughts().get(thoughtId)?.toDomain() ?: return
        if (projectId in t.projectIds) return
        updateThought(t.copy(projectIds = t.projectIds + projectId))
        addRelationship(
            fromId = thoughtId,
            toId = projectId,
            fromKind = GraphKind.thought,
            toKind = GraphKind.project,
            type = RelationType.belongs_to_project,
            explanation = "",
        )
    }

    suspend fun addRelationship(
        fromId: String,
        toId: String,
        fromKind: GraphKind,
        toKind: GraphKind,
        type: RelationType,
        explanation: String,
        provenance: Provenance = Provenance.user,
    ): Relationship? {
        if (fromId == toId) return null
        val existing = db.relationships().all().firstOrNull {
            it.fromId == fromId && it.toId == toId && it.relationType == type.name
        }
        if (existing != null) return existing.toDomain()
        val rel = Relationship(
            id = id("rel"), fromId, toId, fromKind, toKind, type, explanation.trim(), provenance, null, now(),
        )
        db.relationships().upsert(rel.toRow())
        return rel
    }

    suspend fun deleteRelationship(id: String) = db.relationships().delete(id)

    suspend fun addBook(title: String, author: String): ReadingItem {
        val stamp = now()
        val item = ReadingItem(id("bk"), title, author, ReadingStatus.unopened, "", "", "", false, stamp, stamp)
        db.reading().upsert(item.toRow())
        return item
    }

    suspend fun updateBook(item: ReadingItem) {
        db.reading().upsert(item.copy(updatedAt = now()).toRow())
    }

    suspend fun deleteBook(id: String) = db.reading().delete(id)

    suspend fun saveProfile(profile: UserProfile) {
        db.profile().upsert(profile.copy(updatedAt = now()).toRow())
    }

    suspend fun addAttachment(thoughtId: String, uri: Uri, name: String, mime: String, size: Long): AttachmentMeta {
        val row = AttachmentRow(id("att"), thoughtId, name, mime, uri.toString(), now(), size)
        db.attachments().upsert(row)
        return row.toMeta()
    }

    suspend fun deleteAttachment(id: String) = db.attachments().delete(id)

    suspend fun exportJson(): String {
        val file = snapshot()
        return jsonPretty.encodeToString(LabExportFile.serializer(), file)
    }

    suspend fun snapshot(): LabExportFile {
        val profile = db.profile().get()?.toDomain() ?: defaultProfile()
        return LabExportFile(
            format = EXPORT_FORMAT,
            version = EXPORT_VERSION,
            exportedAt = now(),
            thoughts = db.thoughts().all().map { it.toDomain().toDto() },
            projects = db.projects().all().map { it.toDomain().toDto() },
            relationships = db.relationships().all().map { it.toDomain().toDto() },
            sources = db.sources().all().map {
                lab.neurotrino.app.data.export.SourceDto(
                    it.id, it.title, it.authors, it.year, it.publisher, it.doi, it.url, it.sourceType, it.notes, it.accessedAt, it.createdAt,
                )
            },
            readingItems = db.reading().all().map { it.toDomain().toDto() },
            sparks = emptyList(),
            profile = profile.toDto(),
        )
    }

    suspend fun importJson(raw: String, replace: Boolean): Pair<Int, Int> {
        val data = parseExport(raw)
        val backup = snapshot()
        db.meta().upsert(MetaRow("backup:pre-import:${backup.exportedAt}", jsonPretty.encodeToString(LabExportFile.serializer(), backup)))
        if (replace) {
            db.thoughts().clear()
            db.projects().clear()
            db.relationships().clear()
            db.reading().clear()
            db.sources().clear()
        }
        db.thoughts().upsertAll(data.thoughts.map { it.toDomain().toRow() })
        db.projects().upsertAll(data.projects.map { it.toDomain().toRow() })
        db.relationships().upsertAll(data.relationships.map { it.toDomain().toRow() })
        db.reading().upsertAll(data.readingItems.map { it.toDomain().toRow() })
        db.sources().upsertAll(data.sources.map { it.toDomain().toRow() })
        db.profile().upsert(data.profile.toDomain().toRow())
        return data.thoughts.size to data.projects.size
    }

    suspend fun removeSamples() {
        val sampleThoughts = db.thoughts().all().filter { it.isSample }
        val sampleProjects = db.projects().all().filter { it.isSample }
        val ids = (sampleThoughts.map { it.id } + sampleProjects.map { it.id }).toSet()
        sampleThoughts.forEach { db.thoughts().delete(it.id) }
        sampleProjects.forEach { db.projects().delete(it.id) }
        db.relationships().all().filter { it.fromId in ids || it.toId in ids }.forEach { db.relationships().delete(it.id) }
    }

    suspend fun wipe(_unused: Context? = null) {
        db.thoughts().clear()
        db.projects().clear()
        db.relationships().clear()
        db.reading().clear()
        db.sources().clear()
        db.meta().upsert(MetaRow("seeded", ""))
        // re-seed
        db.meta().upsert(MetaRow("seeded_reset", now()))
        val stamp = now()
        db.profile().upsert(defaultProfile().copy(updatedAt = stamp).toRow())
        db.reading().upsertAll(starterLibrary(stamp).map { it.toRow() })
        db.projects().upsert(lab.neurotrino.app.data.db.sampleProject(stamp).toRow())
        db.thoughts().upsertAll(lab.neurotrino.app.data.db.sampleThoughts(stamp).map { it.toRow() })
        db.relationships().upsertAll(lab.neurotrino.app.data.db.sampleRels(stamp).map { it.toRow() })
        db.meta().upsert(MetaRow("seeded", "true"))
    }

    private fun now() = Instant.now().toString()
    private fun id(prefix: String) = "${prefix}_${UUID.randomUUID()}"
}

private fun AttachmentRow.toMeta() = AttachmentMeta(id, thoughtId, name, mime, uri, createdAt, size)
