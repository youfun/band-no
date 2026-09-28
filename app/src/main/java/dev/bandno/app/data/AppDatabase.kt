package dev.bandno.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CallAttemptEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callAttemptDao(): CallAttemptDao
}
