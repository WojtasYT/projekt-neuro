package lab.neurotrino.app.data.repo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import lab.neurotrino.app.data.db.AttachmentRow
import lab.neurotrino.app.data.db.MetaRow
import lab.neurotrino.app.data.db.NeurotrinoDatabase
import lab.neurotrino.app.data.db.defaultProfile
import lab.neurotrino.app.data.db.ensureSeeded
import lab.neurotrino.app.data.db.jsonPretty
import lab.neurotrino.app.data.db.starterLibrary
import lab.neurotrino.app.data.db.toDomain
import lab.neurotrino.app.data.db.toRow
import lab.neurotrino.app.data.export.AttachmentDto
import lab.neurotrino.app.data.export.LabExportFile
import lab.neurotrino.app.data.export.SourceDto
import lab.neurotrino.app.data.export.parseExport
import lab.neurotrino.app.data.export.toDomain
import lab.neurotrino.app.data.export.toDto
import lab.neurotrino.app.domain.AssistantMessage
import lab.neurotrino.app.domain.AssistantMode
import lab.neurotrino.app.domain.AttachmentMeta
import lab.neurotrino.app.domain.Conversation
import lab.neurotrino.app.domain.EXPORT_FORMAT
import lab.neurotrino.app.domain.EXPORT_VERSION
import lab.neurotrino.app.domain.Expansion
import lab.neurotrino.app.domain.GraphKind
import lab.neurotrino.app.domain.HistoryNote
import lab.neurotrino.app.domain.HypothesisFields
import lab.neurotrino.app.domain.ImportSummary
import lab.neurotrino.app.domain.Project
import lab.neurotrino.app.domain.ProjectStatus
import lab.neurotrino.app.domain.Provenance
import lab.neurotrino.app.domain.ReadingItem
import lab.neurotrino.app.domain.ReadingStatus
import lab.neurotrino.app.domain.RelationType
import lab.neurotrino.app.domain.Relationship
import lab.neurotrino.app.domain.SourceItem
import lab.neurotrino.app.domain.Thought
import lab.neurotrino.app.domain.ThoughtStatus
import lab.neurotrino.app.domain.ThoughtType
import lab.neurotrino.app.domain.UserProfile
import lab.neurotrino.app.domain.conversationTitle
import lab.neurotrino.app.domain.previewTitle
import lab.neurotrino.app.domain.splitFragments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.io.File
import java.time.Instant
import java.util.UUID

class LabRepository(
    private val db: NeurotrinoDatabase,
    private val context: Context,
) {
    private val appContext = context.applicationContext

    val thoughts: Flow<List<Thought>> = db.thoughts().observe().map { rows -> rows.map { it.toDomain() } }
    val projects: Flow<List<Project>> = db.projects().observe().map { rows -> rows.map { it.toDomain() } }
    val relationships: Flow<List<Relationship>> = db.relationships().observe().map { rows -> rows.map { it.toDomain() } }
    val reading: Flow<List<ReadingItem>> = db.reading().observe().map { rows -> rows.map { it.toDomain() } }
    val sources: Flow<List<SourceItem>> = db.sources().observe().map { rows -> rows.map { it.toDomain() } }
    val profile: Flow<UserProfile> = db.profile().observe().map { it?.toDomain() ?: defaultProfile() }

    fun thought(id: String): Flow<Thought?> = db.thoughts().observeOne(id).map { it?.toDomain() }
    fun project(id: String): Flow<Project?> = db.projects().observeOne(id).map { it?.toDomain() }
    fun book(id: String): Flow<ReadingItem?> = db.reading().observeOne(id).map { it?.toDomain() }
    fun attachments(thoughtId: String): Flow<List<AttachmentMeta>> =
        db.attachments().observeForThought(thoughtId).map { rows -> rows.map { it.toMeta() } }

    val conversations: Flow<List<Conversation>> = db.conversations().observe().map { rows -> rows.map { it.toDomain() } }

    fun conversation(id: String): Flow<Conversation?> =
        conversations.map { list -> list.find { it.id == id } }

    fun messages(conversationId: String): Flow<List<AssistantMessage>> =
        db.messages().observeForConversation(conversationId).map { rows -> rows.map { it.toDomain() } }

    suspend fun messagesOnce(conversationId: String): List<AssistantMessage> =
        db.messages().forConversation(conversationId).map { it.toDomain() }

    suspend fun conversationOnce(id: String): Conversation? = db.conversations().get(id)?.toDomain()

    /** Ostatnia aktywna rozmowa powiązana z daną myślą (jeśli istnieje). */
    suspend fun latestConversationForThought(thoughtId: String): Conversation? =
        db.conversations().all().map { it.toDomain() }
            .filter { it.sourceThoughtId == thoughtId && !it.archived }
            .maxByOrNull { it.updatedAt }

    suspend fun start() = db.ensureSeeded()

    // ── Capture ────────────────────────────────────────────────────────────────
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

    /**
     * Central merge path. The stored raw capture is immutable: `rawText`,
     * `createdAt` and `provenance` always survive. Everything editable comes
     * from the caller's merged draft. Title falls back to a derived preview
     * only when left blank.
     */
    suspend fun updateThought(thought: Thought) {
        val existing = db.thoughts().get(thought.id)
        val merged = if (existing == null) {
            thought.copy(updatedAt = now())
        } else {
            thought.copy(
                rawText = existing.rawText,
                createdAt = existing.createdAt,
                provenance = runCatching { Provenance.valueOf(existing.provenance) }.getOrDefault(thought.provenance),
                updatedAt = now(),
                title = thought.title.ifBlank { previewTitle(thought.content.ifBlank { existing.rawText }) },
            )
        }
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
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val t = db.thoughts().get(id)?.toDomain() ?: return
        updateThought(
            t.copy(expansions = t.expansions + Expansion(id("ex"), trimmed, provenance, now())),
        )
    }

    suspend fun deleteThought(thoughtId: String) {
        db.attachments().forThought(thoughtId).forEach { deleteAttachmentFile(it.id) }
        db.thoughts().delete(thoughtId)
        db.relationships().deleteTouching(thoughtId)
        db.attachments().deleteForThought(thoughtId)
    }

    // ── Projects ───────────────────────────────────────────────────────────────
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

    suspend fun deleteProject(projectId: String) {
        db.projects().delete(projectId)
        db.thoughts().all().forEach { row ->
            val t = row.toDomain()
            if (projectId in t.projectIds) {
                db.thoughts().upsert(t.copy(projectIds = t.projectIds - projectId, updatedAt = now()).toRow())
            }
        }
        db.relationships().deleteTouching(projectId)
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

    suspend fun detachThoughtFromProject(thoughtId: String, projectId: String) {
        val t = db.thoughts().get(thoughtId)?.toDomain() ?: return
        if (projectId !in t.projectIds) return
        updateThought(t.copy(projectIds = t.projectIds - projectId))
        db.relationships().deleteRelation(thoughtId, projectId, RelationType.belongs_to_project.name)
    }

    // ── Relationships ─────────────────────────────────────────────────────────
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

    // ── Library ────────────────────────────────────────────────────────────────
    suspend fun addBook(title: String, author: String): ReadingItem {
        val stamp = now()
        val item = ReadingItem(id("bk"), title.trim(), author.trim(), ReadingStatus.unopened, "", "", "", false, stamp, stamp)
        db.reading().upsert(item.toRow())
        return item
    }

    suspend fun updateBook(item: ReadingItem) {
        db.reading().upsert(item.copy(updatedAt = now()).toRow())
    }

    suspend fun deleteBook(id: String) {
        db.reading().delete(id)
        db.thoughts().all().forEach { row ->
            val t = row.toDomain()
            if (id in t.readingItemIds) {
                db.thoughts().upsert(t.copy(readingItemIds = t.readingItemIds - id, updatedAt = now()).toRow())
            }
        }
        db.relationships().deleteTouching(id)
    }

    // ── Sources ────────────────────────────────────────────────────────────────
    suspend fun addSource(
        title: String,
        authors: String,
        url: String,
        doi: String,
        notes: String = "",
    ): SourceItem? {
        if (title.isBlank()) return null
        val stamp = now()
        val source = SourceItem(
            id = id("src"),
            title = title.trim(),
            authors = authors.trim(),
            year = "",
            publisher = "",
            doi = doi.trim(),
            url = url.trim(),
            sourceType = when {
                doi.isNotBlank() -> "publication"
                url.contains("pubmed", ignoreCase = true) -> "publication"
                url.isNotBlank() -> "web"
                else -> "other"
            },
            notes = notes.trim(),
            accessedAt = if (url.isNotBlank() || doi.isNotBlank()) stamp else "",
            createdAt = stamp,
        )
        db.sources().upsert(source.toRow())
        return source
    }

    suspend fun attachSourceToThought(thoughtId: String, sourceId: String) {
        val t = db.thoughts().get(thoughtId)?.toDomain() ?: return
        if (sourceId !in t.sourceIds) {
            updateThought(t.copy(sourceIds = t.sourceIds + sourceId))
        }
        addRelationship(
            fromId = thoughtId,
            toId = sourceId,
            fromKind = GraphKind.thought,
            toKind = GraphKind.source,
            type = RelationType.source_for,
            explanation = "",
            provenance = Provenance.source,
        )
    }

    suspend fun detachSourceFromThought(thoughtId: String, sourceId: String) {
        val t = db.thoughts().get(thoughtId)?.toDomain() ?: return
        updateThought(t.copy(sourceIds = t.sourceIds - sourceId))
        db.relationships().deleteRelation(thoughtId, sourceId, RelationType.source_for.name)
    }

    suspend fun deleteSource(sourceId: String) {
        db.sources().delete(sourceId)
        db.thoughts().all().forEach { row ->
            val t = row.toDomain()
            if (sourceId in t.sourceIds) {
                db.thoughts().upsert(t.copy(sourceIds = t.sourceIds - sourceId, updatedAt = now()).toRow())
            }
        }
        db.relationships().deleteTouching(sourceId)
    }

    suspend fun saveProfile(profile: UserProfile) {
        db.profile().upsert(profile.copy(updatedAt = now()).toRow())
    }

    // ── Attachments ───────────────────────────────────────────────────────────
    /**
     * Copies the picked image into app-private storage so it survives process
     * death, app restarts and permission revocation. Returns the stored record.
     */
    suspend fun addAttachment(thoughtId: String, source: Uri, fallbackName: String, mime: String, size: Long): AttachmentMeta =
        withContext(Dispatchers.IO) {
            val stamp = now()
            val attachmentId = id("att")
            val (displayName, resolvedSize) = queryDisplayName(source)?.let { it to (size.takeIf { s -> s > 0 } ?: -1L) }
                ?: (fallbackName to size)
            val dir = File(appContext.filesDir, "attachments").apply { mkdirs() }
            val safeName = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(72).ifBlank { "plik" }
            val dest = File(dir, "$attachmentId-$safeName")
            appContext.contentResolver.openInputStream(source)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalArgumentException("Nie udało się odczytać wybranego pliku.")
            val meta = AttachmentMeta(
                id = attachmentId,
                thoughtId = thoughtId,
                name = displayName,
                mime = mime.ifBlank { "image/*" },
                uri = dest.toURI().toString(),
                createdAt = stamp,
                size = dest.length().takeIf { it > 0 } ?: resolvedSize.coerceAtLeast(0),
            )
            db.attachments().upsert(meta.toRow())
            meta
        }

    private fun queryDisplayName(uri: Uri): String? {
        if (uri.scheme != "content") return null
        return runCatching {
            appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    suspend fun deleteAttachment(attachmentId: String) {
        deleteAttachmentFile(attachmentId)
        db.attachments().delete(attachmentId)
    }

    private suspend fun deleteAttachmentFile(attachmentId: String) {
        val row = db.attachments().get(attachmentId) ?: return
        runCatching {
            val path = Uri.parse(row.uri).path
            if (path != null) {
                val file = File(path)
                val root = File(appContext.filesDir, "attachments").canonicalFile
                if (file.canonicalFile.path.startsWith(root.path)) file.delete()
            }
        }
    }

    // ── Export / import ───────────────────────────────────────────────────────
    // ── Asystent: rozmowy (V4 P0) ─────────────────────────────────────────────
    suspend fun createConversation(mode: AssistantMode, projectId: String? = null, sourceThoughtId: String? = null): Conversation {
        val stamp = now()
        val convo = Conversation(
            id = id("cv"), title = "Nowa rozmowa", projectId = projectId,
            sourceThoughtId = sourceThoughtId, mode = mode, archived = false,
            createdAt = stamp, updatedAt = stamp,
        )
        db.conversations().upsert(convo.toRow())
        return convo
    }

    suspend fun renameConversation(id: String, title: String) {
        val clean = title.trim().ifBlank { "Nowa rozmowa" }.take(120)
        db.conversations().rename(id, clean, now())
    }

    suspend fun setConversationMode(id: String, mode: AssistantMode) {
        db.conversations().setMode(id, mode.name, now())
    }

    suspend fun setConversationArchived(id: String, archived: Boolean) {
        db.conversations().setArchived(id, archived, now())
    }

    suspend fun deleteConversation(id: String) {
        db.messages().deleteForConversation(id) // jawnie — niezależnie od ustawienia FK pragma
        db.conversations().delete(id)
    }

    /** Zapisuje wiadomość; jeśli rozmowa ma jeszcze domyślny tytuł, ustala go z 1. wypowiedzi użytkownika. */
    suspend fun addConversationMessage(
        conversationId: String,
        role: String,
        content: String,
        provenance: Provenance,
        model: String? = null,
    ): AssistantMessage {
        val stamp = now()
        val msg = AssistantMessage(
            id = id("msg"), conversationId = conversationId, role = role,
            content = content, model = model, provenance = provenance, createdAt = stamp,
        )
        db.messages().upsert(msg.toRow())
        db.conversations().touch(conversationId, stamp)
        if (role == "user") {
            db.conversations().get(conversationId)?.let { row ->
                if (row.title == "Nowa rozmowa") {
                    db.conversations().rename(conversationId, conversationTitle(content), stamp)
                }
            }
        }
        return msg
    }

    /** Jawnie zapisuje fragment odpowiedzi AI jako koncepcję użytkownika (nigdy automatycznie). */
    suspend fun saveAiTextAsThought(content: String, projectId: String? = null, title: String? = null): Thought {
        val stamp = now()
        val clean = content.trim()
        val thought = Thought(
            id = id("th"),
            title = title?.take(72)?.ifBlank { null } ?: previewTitle(clean),
            rawText = clean,
            content = clean,
            types = listOf(ThoughtType.other),
            status = ThoughtStatus.seed,
            tags = listOf("asystent"),
            createdAt = stamp,
            updatedAt = stamp,
            lastOpenedAt = stamp,
            provenance = Provenance.ai, // zachowujemy proweniencję — to dzieło modelu
            projectIds = listOfNotNull(projectId),
            sourceIds = emptyList(),
            readingItemIds = emptyList(),
        )
        db.thoughts().upsert(thought.toRow())
        return thought
    }

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
                SourceDto(it.id, it.title, it.authors, it.year, it.publisher, it.doi, it.url, it.sourceType, it.notes, it.accessedAt, it.createdAt)
            },
            readingItems = db.reading().all().map { it.toDomain().toDto() },
            sparks = emptyList(),
            conversations = db.conversations().all().map { it.toDomain().toDto() },
            messages = db.messages().all().map { it.toDomain().toDto() },
            attachments = attachmentDtos(),
            profile = profile.toDto(),
        )

    /** Załączniki do eksportu: treść binarna jako base64; brakujący plik = omittedReason (jawnie). */
    private suspend fun attachmentDtos(): List<AttachmentDto> = withContext(Dispatchers.IO) {
        db.attachments().all().map { row ->
            val file = runCatching {
                val uri = java.net.URI(row.uri)
                File(uri)
            }.getOrNull()
            val bytes = file?.takeIf { it.isFile && it.length() > 0 }?.readBytes()
            AttachmentDto(
                id = row.id, thoughtId = row.thoughtId, name = row.name, mime = row.mime,
                size = row.size, createdAt = row.createdAt,
                dataBase64 = bytes?.let { java.util.Base64.getEncoder().encodeToString(it) },
                omittedReason = if (bytes == null) "plik nie istnieje lub jest pusty" else null,
            )
        }
    }
    }

    suspend fun importJson(raw: String, replace: Boolean): ImportSummary {
        val data = parseExport(raw)
        val backup = snapshot()
        val backupKey = "backup:pre-import:${backup.exportedAt}"
        db.meta().upsert(MetaRow(backupKey, jsonPretty.encodeToString(LabExportFile.serializer(), backup)))
        pruneImportBackups(keep = 1)
        // Rozmowy/wiadomości: integralność referencji sprawdzona już w parseExport.
        // Przy replace kasuje cascade; przy scalaniu dopinsujemy do istniejących (FK pilnuje bazy).
        val existingConvoIds = if (replace) emptySet() else db.conversations().all().map { it.id }.toSet()
        val knownConvoIds = existingConvoIds + data.conversations.map { it.id }.toSet()
        val missing = data.messages.map { it.conversationId }.filter { it !in knownConvoIds }.distinct()
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException("Wiadomości odwołują się do nieznanych rozmów: ${missing.take(3).joinToString(", ")}")
        }
        if (replace) {
            withContext(Dispatchers.IO) {
                File(appContext.filesDir, "attachments").deleteRecursively()
            }
            db.thoughts().clear()
            db.projects().clear()
            db.relationships().clear()
            db.reading().clear()
            db.sources().clear()
            db.attachments().clear()
            db.messages().clear()
            db.conversations().clear()
        }
        db.conversations().upsertAll(data.conversations.map { it.toDomain().toRow() })
        db.messages().upsertAll(data.messages.map { it.toDomain().toRow() })
        db.thoughts().upsertAll(data.thoughts.map { it.toDomain().toRow() })
        val restoredAttachments = restoreAttachments(data.attachments)
        db.projects().upsertAll(data.projects.map { it.toDomain().toRow() })
        db.relationships().upsertAll(data.relationships.map { it.toDomain().toRow() })
        db.reading().upsertAll(data.readingItems.map { it.toDomain().toRow() })
        db.sources().upsertAll(data.sources.map { it.toDomain().toRow() })
        db.profile().upsert(data.profile.toDomain().toRow())
        return ImportSummary(
            thoughts = data.thoughts.size,
            projects = data.projects.size,
            conversations = data.conversations.size,
            messages = data.messages.size,
            attachments = restoredAttachments,
        )
    }

    /** Przywróć załączniki z eksportu (binaria base64 → pliki w filesDir/attachments + rekordy). */
    private suspend fun restoreAttachments(attachments: List<AttachmentDto>): Int =
        withContext(Dispatchers.IO) {
            var restored = 0
            val dir = File(appContext.filesDir, "attachments").apply { mkdirs() }
            for (dto in attachments) {
                val payload = dto.dataBase64 ?: continue // jawnie pominięte przy eksporcie
                val bytes = runCatching { java.util.Base64.getDecoder().decode(payload) }.getOrNull() ?: continue
                val safeName = dto.name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(72).ifBlank { "plik" }
                val dest = File(dir, "${dto.id}-$safeName")
                dest.writeBytes(bytes)
                db.attachments().upsert(
                    lab.neurotrino.app.data.db.AttachmentRow(
                        id = dto.id, thoughtId = dto.thoughtId, name = dto.name, mime = dto.mime,
                        uri = dest.toURI().toString(), createdAt = dto.createdAt, size = bytes.size.toLong(),
                    )
                )
                restored++
            }
            restored
        }

    private suspend fun pruneImportBackups(keep: Int) {
        val keys = db.meta().backupKeys()
        if (keys.size > keep) {
            keys.sorted().dropLast(keep).forEach { db.meta().deleteKey(it) }
        }
    }

    suspend fun removeSamples() {
        val sampleThoughts = db.thoughts().all().filter { it.isSample }
        val sampleProjects = db.projects().all().filter { it.isSample }
        sampleThoughts.forEach { deleteThought(it.id) }
        sampleProjects.forEach { deleteProject(it.id) }
    }

    /**
     * Clears all user data and resets to a clean laboratory: empty database,
     * default profile, no sample content. The starter bookshelf is kept only
     * on first launch, not resurrected here.
     */
    suspend fun wipe() {
        withContext(Dispatchers.IO) {
            File(appContext.filesDir, "attachments").deleteRecursively()
        }
        db.thoughts().clear()
        db.projects().clear()
        db.relationships().clear()
        db.reading().clear()
        db.sources().clear()
        db.attachments().clear()
        db.messages().clear()
        db.conversations().clear()
        db.meta().clear()
        // Prevent the first-launch seeder from resurrecting sample content.
        db.meta().upsert(MetaRow("seeded", "true"))
        db.profile().upsert(defaultProfile().copy(updatedAt = now()).toRow())
    }

    private fun now() = Instant.now().toString()
    private fun id(prefix: String) = "${prefix}_${UUID.randomUUID()}"
}

private fun AttachmentRow.toMeta() = AttachmentMeta(id, thoughtId, name, mime, uri, createdAt, size)
