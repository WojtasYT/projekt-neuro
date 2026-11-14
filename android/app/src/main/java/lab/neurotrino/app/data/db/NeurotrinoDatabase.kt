package lab.neurotrino.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import lab.neurotrino.app.domain.AssistantMode
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

@Database(
    entities = [
        ThoughtRow::class, ProjectRow::class, RelationshipRow::class, ReadingRow::class,
        SourceRow::class, AttachmentRow::class, ProfileRow::class, MetaRow::class,
        ConversationRow::class, MessageRow::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class NeurotrinoDatabase : RoomDatabase() {
    abstract fun thoughts(): ThoughtDao
    abstract fun projects(): ProjectDao
    abstract fun relationships(): RelationshipDao
    abstract fun reading(): ReadingDao
    abstract fun sources(): SourceDao
    abstract fun attachments(): AttachmentDao
    abstract fun profile(): ProfileDao
    abstract fun meta(): MetaDao
    abstract fun conversations(): ConversationDao
    abstract fun messages(): MessageDao

    companion object {
        private val seeding = AtomicBoolean(false)

        fun create(context: Context): NeurotrinoDatabase =
            Room.databaseBuilder(context, NeurotrinoDatabase::class.java, "neurotrino-lab.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

/**
 * Room v1 → v2: dokłada rozmowy i ich wiadomości (V4 P0 — pamięć asystenta).
 * DDL musi ODPOWIADAĆ DOKŁADNIE encjiom (Room waliduje schemat przy otwarciu).
 * Brak fallbackToDestructiveMigration — dane użytkownika są nieodwracalne.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `conversations` (" +
                "`id` TEXT NOT NULL, `title` TEXT NOT NULL, `projectId` TEXT, " +
                "`sourceThoughtId` TEXT, `mode` TEXT NOT NULL, `archived` INTEGER NOT NULL, " +
                "`createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `messages` (" +
                "`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `role` TEXT NOT NULL, " +
                "`content` TEXT NOT NULL, `model` TEXT, `provenance` TEXT NOT NULL, `createdAt` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_updatedAt` ON `conversations` (`updatedAt`)")
    }
}

suspend fun NeurotrinoDatabase.ensureSeeded() {
    if (meta().get("seeded") != null) {
        if (profile().get() == null) profile().upsert(defaultProfile().toRow())
        return
    }
    val stamp = Instant.now().toString()
    profile().upsert(defaultProfile().copy(updatedAt = stamp).toRow())
    reading().upsertAll(starterLibrary(stamp).map { it.toRow() })
    projects().upsert(sampleProject(stamp).toRow())
    thoughts().upsertAll(sampleThoughts(stamp).map { it.toRow() })
    relationships().upsertAll(sampleRels(stamp).map { it.toRow() })
    meta().upsert(MetaRow("seeded", "true"))
}

fun defaultProfile() = UserProfile(
    interests = listOf("neurobiologia", "neurochemia", "percepcja", "kosmologia", "inżynieria", "filozofia"),
    theme = "dark",
    aiDefaultMode = AssistantMode.impuls,
)

fun starterLibrary(stamp: String) = listOf(
    ReadingItem("book-kwantechizm", "Kwantechizm 2.0", "Andrzej Dragan", ReadingStatus.unopened, "", "", "", true, stamp, stamp),
    ReadingItem("book-krotka-historia", "Krótka historia czasu. Ilustrowana", "Stephen Hawking", ReadingStatus.unopened, "", "", "", true, stamp, stamp),
    ReadingItem("book-wszechswiat", "Wszechświat w skorupce orzecha", "Stephen Hawking", ReadingStatus.exploring, "Kilka stron; pojawiły się własne przemyślenia.", "", "", true, stamp, stamp),
    ReadingItem("book-psychodeliki", "Psychodeliki", "David Nutt", ReadingStatus.unopened, "", "", "", true, stamp, stamp),
    ReadingItem("book-neuromania", "Neuromania", "Albert Moukheiber", ReadingStatus.exploring, "Około połowy.", "", "", true, stamp, stamp),
    ReadingItem("book-pulapki", "Pułapki myślenia", "Daniel Kahneman", ReadingStatus.unopened, "", "", "", true, stamp, stamp),
    ReadingItem("book-dose", "DOSE", "TJ Power", ReadingStatus.finished, "Przeczytana niedawno, nieregularnie, przez dłuższy czas.", "", "", true, stamp, stamp),
)

fun sampleProject(stamp: String) = Project(
    id = "sample-project-neurotrino",
    title = "Neurotrino",
    purpose = "Środowisko do nieliniowego łączenia myśli, bez oddawania autorstwa.",
    description = "Przykładowy projekt startowy. Możesz go zostawić, przepisać albo usunąć.",
    status = ProjectStatus.incubating,
    nextStep = "Zapisać jedną surową obserwację, która ostatnio została w głowie.",
    createdAt = stamp,
    updatedAt = stamp,
    isSample = true,
    history = listOf(HistoryNote(stamp, "Utworzono jako treść przykładową.")),
)

fun sampleThoughts(stamp: String) = listOf(
    Thought(
        id = "sample-thought-clock",
        title = "Zegar neuronalny — szkic",
        rawText = "A gdyby subiektywne poczucie upływu czasu zależało od liczby i rodzaju zdarzeń neuronalnych, nowości, rutyny i tła neurochemicznego? To nie jest twierdzenie. To pytanie, które chcę utrzymać przy życiu.",
        content = "A gdyby subiektywne poczucie upływu czasu zależało od liczby i rodzaju zdarzeń neuronalnych, nowości, rutyny i tła neurochemicznego? To nie jest twierdzenie. To pytanie, które chcę utrzymać przy życiu.",
        types = listOf(ThoughtType.hypothesis, ThoughtType.question),
        status = ThoughtStatus.paused,
        tags = listOf("czas", "neurony", "nowość"),
        createdAt = stamp, updatedAt = stamp, lastOpenedAt = stamp,
        provenance = Provenance.sample,
        projectIds = emptyList(), sourceIds = emptyList(), readingItemIds = emptyList(),
        isSample = true,
        hypothesis = HypothesisFields(
            observation = "Godzina w nowym miejscu potrafi być „grubsza” niż godzina w rutynie.",
            proposal = "Subiektywne tempo czasu może skalować się z gęstością i rodzajem zdarzeń neuronalnych.",
            assumptions = "Że istnieje mierzalny odpowiednik „zdarzenia”.",
            known = "Efekty nowości są opisywane; jednolity zegar zdarzeń — nie.",
            nextStep = "Rozdzielić trzy warstwy: chwila, tempo, ocena wsteczna.",
        ),
    ),
    Thought(
        id = "sample-thought-lab",
        title = "Po co to laboratorium",
        rawText = "Chcę miejsce, w którym mogę złapać myśl zanim zniknie, wrócić do niej bez wstydu i połączyć ją z inną — nawet jeśli nic z tego nie będzie „dziełem”.",
        content = "Chcę miejsce, w którym mogę złapać myśl zanim zniknie, wrócić do niej bez wstydu i połączyć ją z inną — nawet jeśli nic z tego nie będzie „dziełem”.",
        types = listOf(ThoughtType.reflection, ThoughtType.project_note),
        status = ThoughtStatus.seed,
        tags = listOf("neurotrino", "proces"),
        createdAt = stamp, updatedAt = stamp, lastOpenedAt = stamp,
        provenance = Provenance.sample,
        projectIds = listOf("sample-project-neurotrino"),
        sourceIds = emptyList(), readingItemIds = emptyList(),
        isSample = true,
    ),
)

fun sampleRels(stamp: String) = listOf(
    Relationship(
        id = "sample-rel-lab-project",
        fromId = "sample-thought-lab",
        toId = "sample-project-neurotrino",
        fromKind = GraphKind.thought,
        toKind = GraphKind.project,
        relationType = RelationType.belongs_to_project,
        explanation = "Notatka o sensie laboratorium należy do projektu Neurotrino.",
        provenance = Provenance.sample,
        createdAt = stamp,
    ),
)
