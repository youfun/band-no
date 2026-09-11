package dev.bandno.app.ui

import android.content.Context
import dev.bandno.app.R
import dev.bandno.app.data.CallAttemptEntity
import dev.bandno.decision.AllowWindow
import dev.bandno.decision.DecisionAction
import dev.bandno.decision.NumberDisplay
import dev.bandno.decision.RuleHit
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DateTimeFormatterLocal: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

fun LocalTime.formatHm(): String = format(TimeFormatter)

fun Instant.formatLocal(zoneId: ZoneId = ZoneId.systemDefault()): String =
    atZone(zoneId).format(DateTimeFormatterLocal)

fun CallAttemptEntity.displayNumber(mask: Boolean): String {
    val raw = displayNumber.ifBlank { normalizedNumber }
    if (raw.isBlank()) return ""
    return if (mask) NumberDisplay.mask(raw.filter { it.isDigit() }.ifEmpty { raw }) else raw
}

fun actionLabel(context: Context, action: String): String = when (action) {
    DecisionAction.ALLOW.name -> context.getString(R.string.logs_action_allow)
    DecisionAction.SILENCE.name -> context.getString(R.string.logs_action_silence)
    DecisionAction.REJECT.name -> context.getString(R.string.logs_action_reject)
    else -> action
}

fun ruleLabel(context: Context, ruleHit: String): String = when (ruleHit) {
    RuleHit.PRIVATE_NUMBER.name -> context.getString(R.string.logs_rule_private)
    RuleHit.CONTACT.name -> context.getString(R.string.logs_rule_contact)
    RuleHit.PREFIX_BLOCK.name -> context.getString(R.string.logs_rule_prefix)
    RuleHit.ALLOW_WINDOW.name -> context.getString(R.string.logs_rule_window)
    "R1_ALLOW_WINDOW" -> context.getString(R.string.logs_rule_window_legacy)
    "R2_IMPORTANT_WINDOW" -> context.getString(R.string.logs_rule_window_legacy)
    RuleHit.R3_REPEAT_CALL.name -> context.getString(R.string.logs_rule_repeat)
    RuleHit.DEFAULT_BLOCK.name -> context.getString(R.string.logs_rule_block)
    else -> ruleHit
}

fun formatAllowWindow(window: AllowWindow): String {
    val span = "${window.start.formatHm()}–${window.end.formatHm()}"
    val note = window.note.trim()
    return if (note.isEmpty()) span else "$span · $note"
}

fun formatAllowWindowsSummary(windows: List<AllowWindow>): String =
    windows.joinToString("、") { formatAllowWindow(it) }

fun formatBlockedPrefixesSummary(prefixes: List<String>): String =
    prefixes.joinToString("、")
