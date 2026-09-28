package dev.bandno.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallAttemptDao {
    @Query(
        """
        SELECT * FROM call_attempts
        WHERE normalized_number = :number AND timestamp_epoch_millis >= :sinceMillis
        ORDER BY timestamp_epoch_millis DESC
        """,
    )
    fun listSinceSync(number: String, sinceMillis: Long): List<CallAttemptEntity>

    @Insert
    fun insertSync(entity: CallAttemptEntity)

    @Query("SELECT * FROM call_attempts ORDER BY timestamp_epoch_millis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<CallAttemptEntity>>

    @Query(
        """
        SELECT * FROM call_attempts
        WHERE timestamp_epoch_millis >= :sinceMillis
        ORDER BY timestamp_epoch_millis DESC
        """,
    )
    fun observeSince(sinceMillis: Long): Flow<List<CallAttemptEntity>>

    @Query("DELETE FROM call_attempts")
    suspend fun deleteAll()

    @Query("DELETE FROM call_attempts WHERE timestamp_epoch_millis < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long)
}
