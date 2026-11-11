package lab.neurotrino.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "thoughts")
data class ThoughtRow(
    @PrimaryKey val id: String,
    val title: String,
    val rawText: String,
    val content: String,
    val typesJson: String,
    val status: String,
    val tagsJson: String,
    val createdAt: String,
    val updatedAt: String,
    val lastOpenedAt: String,
    val provenance: String,
    val projectIdsJson: String,
    val sourceIdsJson: String,
    val readingItemIdsJson: String,
    val isSample: Boolean,
    val hypothesisJson: String,
    val expansionsJson: String,
)

@Entity(tableName = "projects")
data class ProjectRow(
    @PrimaryKey val id: String,
    val title: String,
    val purpose: String,
    val description: String,
    val status: String,
    val nextStep: String,
    val createdAt: String,
    val updatedAt: String,
    val isSample: Boolean,
    val historyJson: String,
)

@Entity(tableName = "relationships")
data class RelationshipRow(
    @PrimaryKey val id: String,
    val fromId: String,
    val toId: String,
    val fromKind: String,
    val toKind: String,
    val relationType: String,
    val explanation: String,
    val provenance: String,
    val confidence: String?,
    val createdAt: String,
)

@Entity(tableName = "reading")
data class ReadingRow(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val status: String,
    val progressNote: String,
    val currentPage: String,
    val notes: String,
    val isStarter: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Entity(tableName = "sources")
data class SourceRow(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,
    val year: String,
    val publisher: String,
    val doi: String,
    val url: String,
    val sourceType: String,
    val notes: String,
    val accessedAt: String,
    val createdAt: String,
)

@Entity(tableName = "attachments")
data class AttachmentRow(
    @PrimaryKey val id: String,
    val thoughtId: String,
    val name: String,
    val mime: String,
    val uri: String,
    val createdAt: String,
    val size: Long,
)

@Entity(tableName = "profile")
data class ProfileRow(
    @PrimaryKey val id: String = "local",
    val displayName: String,
    val interestsJson: String,
    val currentFocus: String,
    val onboarded: Boolean,
    val theme: String,
    val aiDefaultMode: String,
    val gatewayUrl: String,
    val model: String,
    val remindersEnabled: Boolean,
    val lastMindState: String?,
    val notesToSelf: String,
    val updatedAt: String,
)

@Entity(tableName = "meta")
data class MetaRow(
    @PrimaryKey val key: String,
    val value: String,
)
