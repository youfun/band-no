package dev.bandno.decision

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

enum class BlockAction {
    SILENCE,
    REJECT,
}

enum class DecisionAction {
    ALLOW,
    SILENCE,
    REJECT,
}

enum class RuleHit {
    PRIVATE_NUMBER,
    CONTACT,
    PREFIX_BLOCK,
    ALLOW_WINDOW,
    R3_REPEAT_CALL,
    DEFAULT_BLOCK,
}

enum class PrivateNumberPolicy {
    ALLOW,
    FOLLOW_RULES,
}

data class AllowWindow(
    val start: LocalTime,
    val end: LocalTime,
    val note: String = "",
)

data class ScreenSettings(
    val allowWindowsEnabled: Boolean = true,
    val allowWindows: List<AllowWindow> = listOf(AllowWindowsMigration.NewDefaultWindow),
    val r3Enabled: Boolean = true,
    val r3IntervalMinutes: Int = 3,
    val r3RequireFirstBlocked: Boolean = false,
    val alwaysAllowContacts: Boolean = true,
    val blockAction: BlockAction = BlockAction.SILENCE,
    val privateNumberPolicy: PrivateNumberPolicy = PrivateNumberPolicy.ALLOW,
    val logRetentionDays: Int = 14,
    val blockedPrefixes: List<String> = emptyList(),
) {
    init {
        require(r3IntervalMinutes in 1..180) { "r3IntervalMinutes must be 1..180" }
        require(logRetentionDays in 1..365) { "logRetentionDays must be 1..365" }
    }

    companion object {
        val Default: ScreenSettings = ScreenSettings()
    }
}

data class PriorAttempt(
    val at: Instant,
    val blocked: Boolean,
)

data class IncomingCall(
    val now: Instant,
    val zoneId: ZoneId,
    val isPrivateOrUnknown: Boolean,
    val isContact: Boolean,
    val priorAttempts: List<PriorAttempt>,
    val normalizedNumber: String? = null,
)

data class NormalizedNumber(
    val digits: String,
    val international: Boolean,
)

data class ScreenDecision(
    val action: DecisionAction,
    val ruleHit: RuleHit,
) {
    val allowed: Boolean get() = action == DecisionAction.ALLOW
}
