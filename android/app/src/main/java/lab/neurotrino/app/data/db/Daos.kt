package lab.neurotrino.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtDao {
    @Query("SELECT * FROM thoughts")
    fun observe(): Flow<List<ThoughtRow>>

    @Query("SELECT * FROM thoughts")
    suspend fun all(): List<ThoughtRow>

    @Query("SELECT * FROM thoughts WHERE id = :id")
    suspend fun get(id: String): ThoughtRow?

    @Query("SELECT * FROM thoughts WHERE id = :id")
    fun observeOne(id: String): Flow<ThoughtRow?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ThoughtRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<ThoughtRow>)

    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM thoughts")
    suspend fun clear()

    @Query("DELETE FROM thoughts WHERE isSample = 1")
    suspend fun deleteSamples()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun observe(): Flow<List<ProjectRow>>

    @Query("SELECT * FROM projects")
    suspend fun all(): List<ProjectRow>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeOne(id: String): Flow<ProjectRow?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun get(id: String): ProjectRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ProjectRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<ProjectRow>)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM projects")
    suspend fun clear()

    @Query("DELETE FROM projects WHERE isSample = 1")
    suspend fun deleteSamples()
}

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships")
    fun observe(): Flow<List<RelationshipRow>>

    @Query("SELECT * FROM relationships")
    suspend fun all(): List<RelationshipRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: RelationshipRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<RelationshipRow>)

    @Query("DELETE FROM relationships WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM relationships WHERE fromId = :id OR toId = :id")
    suspend fun deleteTouching(id: String)

    @Query("DELETE FROM relationships WHERE fromId = :fromId AND toId = :toId AND relationType = :type")
    suspend fun deleteRelation(fromId: String, toId: String, type: String)

    @Query("DELETE FROM relationships")
    suspend fun clear()
}

@Dao
interface ReadingDao {
    @Query("SELECT * FROM reading")
    fun observe(): Flow<List<ReadingRow>>

    @Query("SELECT * FROM reading")
    suspend fun all(): List<ReadingRow>

    @Query("SELECT * FROM reading WHERE id = :id")
    fun observeOne(id: String): Flow<ReadingRow?>

    @Query("SELECT * FROM reading WHERE id = :id")
    suspend fun get(id: String): ReadingRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ReadingRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<ReadingRow>)

    @Query("DELETE FROM reading WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM reading")
    suspend fun clear()
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY createdAt DESC")
    fun observe(): Flow<List<SourceRow>>

    @Query("SELECT * FROM sources")
    suspend fun all(): List<SourceRow>

    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun get(id: String): SourceRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: SourceRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<SourceRow>)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sources")
    suspend fun clear()
}

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE thoughtId = :thoughtId")
    fun observeForThought(thoughtId: String): Flow<List<AttachmentRow>>

    @Query("SELECT * FROM attachments WHERE thoughtId = :thoughtId")
    suspend fun forThought(thoughtId: String): List<AttachmentRow>

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun get(id: String): AttachmentRow?

    @Query("SELECT * FROM attachments")
    suspend fun all(): List<AttachmentRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: AttachmentRow)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM attachments WHERE thoughtId = :thoughtId")
    suspend fun deleteForThought(thoughtId: String)

    @Query("DELETE FROM attachments")
    suspend fun clear()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 'local'")
    fun observe(): Flow<ProfileRow?>

    @Query("SELECT * FROM profile WHERE id = 'local'")
    suspend fun get(): ProfileRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ProfileRow)
}

@Dao
interface MetaDao {
    @Query("SELECT * FROM meta WHERE `key` = :key")
    suspend fun get(key: String): MetaRow?

    @Query("SELECT `key` FROM meta WHERE `key` LIKE 'backup:pre-import:%' ORDER BY `key`")
    suspend fun backupKeys(): List<String>

    @Query("DELETE FROM meta WHERE `key` = :key")
    suspend fun deleteKey(key: String)

    @Query("DELETE FROM meta")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: MetaRow)
}
