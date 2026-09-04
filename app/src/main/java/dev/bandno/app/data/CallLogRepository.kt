package dev.bandno.app.data

import dev.bandno.decision.DecisionAction
import dev.bandno.decision.PriorAttempt
import dev.bandno.decision.ScreenDecision
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class CallLogRepository(
    private val dao: CallAttemptDao,
) {
    fun priorsSince(normalizedNumber: String, since: Instant): List<PriorAttempt> {
        if (normalizedNumber.isEmpty()) return emptyList()
        return dao.listSinceSync(normalizedNumber, since.toEpochMilli()).map { row ->
            PriorAttempt(
                at = Instant.ofEpochMilli(row.timestampEpochMillis),
                blocked = row.action != DecisionAction.ALLOW.name,
            )
        }
    }

    fun recordSync(
        normalizedNumber: String,
        displayNumber: String,
        timestamp: Instant,
        decision: ScreenDecision,
        isContact: Boolean,
    ) {
        dao.insertSync(
            CallAttemptEntity(
                normalizedNumber = normalizedNumber,
                displayNumber = displayNumber,
                timestampEpochMillis = timestamp.toEpochMilli(),
                action = decision.action.name,
                ruleHit = decision.ruleHit.name,
                isContact = isContact,
            ),
        )
    }

    fun observeRecent(): Flow<List<CallAttemptEntity>> = dao.observeRecent()

    fun observeSince(since: Instant): Flow<List<CallAttemptEntity>> =
        dao.observeSince(since.toEpochMilli())

    suspend fun clearAll() {
        dao.deleteAll()
    }

    suspend fun purgeOlderThan(cutoff: Instant) {
        dao.deleteOlderThan(cutoff.toEpochMilli())
    }
}
