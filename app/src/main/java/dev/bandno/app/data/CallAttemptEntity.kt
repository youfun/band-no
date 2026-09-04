package dev.bandno.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_attempts",
    indices = [
        Index(value = ["normalized_number", "timestamp_epoch_millis"]),
        Index(value = ["timestamp_epoch_millis"]),
    ],
)
data class CallAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "normalized_number") val normalizedNumber: String,
    @ColumnInfo(name = "display_number") val displayNumber: String,
    @ColumnInfo(name = "timestamp_epoch_millis") val timestampEpochMillis: Long,
    @ColumnInfo(name = "action") val action: String,
    @ColumnInfo(name = "rule_hit") val ruleHit: String,
    @ColumnInfo(name = "is_contact") val isContact: Boolean,
)
