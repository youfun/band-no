package dev.bandno.decision

import java.time.LocalTime

data class AllowWindowsState(
    val enabled: Boolean,
    val windows: List<AllowWindow>,
)

/**
 * Maps stored preferences to allow-windows.
 *
 * - New installs (no new key, no legacy R1/R2 keys): `18:00–22:00` / 「下班」.
 * - After the new key is written: decode it.
 * - Legacy installs: enabled R1 becomes one window; enabled R2 is added only
 *   when it is not already fully covered by that R1 window.
 */
object AllowWindowsMigration {
    val NewDefaultWindow: AllowWindow = AllowWindow(
        start = LocalTime.of(18, 0),
        end = LocalTime.of(22, 0),
        note = "下班",
    )

    val LegacyR1Start: LocalTime = LocalTime.of(18, 0)
    val LegacyR1End: LocalTime = LocalTime.of(9, 0)
    val LegacyR2Start: LocalTime = LocalTime.of(19, 0)
    val LegacyR2End: LocalTime = LocalTime.of(20, 0)

    fun resolve(
        encodedWindows: String?,
        windowsEnabled: Boolean?,
        hasLegacyKeys: Boolean,
        r1Enabled: Boolean?,
        r1StartMin: Int?,
        r1EndMin: Int?,
        r2Enabled: Boolean?,
        r2StartMin: Int?,
        r2EndMin: Int?,
    ): AllowWindowsState {
        if (encodedWindows != null) {
            val windows = AllowWindowFormat.decode(encodedWindows)
                .ifEmpty { listOf(NewDefaultWindow) }
            return AllowWindowsState(enabled = windowsEnabled ?: true, windows = windows)
        }
        if (hasLegacyKeys) {
            return fromLegacy(
                r1Enabled = r1Enabled ?: true,
                r1Start = minuteOfDayToTime(r1StartMin, LegacyR1Start),
                r1End = minuteOfDayToTime(r1EndMin, LegacyR1End),
                r2Enabled = r2Enabled ?: true,
                r2Start = minuteOfDayToTime(r2StartMin, LegacyR2Start),
                r2End = minuteOfDayToTime(r2EndMin, LegacyR2End),
            )
        }
        return AllowWindowsState(enabled = true, windows = listOf(NewDefaultWindow))
    }

    fun fromLegacy(
        r1Enabled: Boolean,
        r1Start: LocalTime,
        r1End: LocalTime,
        r2Enabled: Boolean,
        r2Start: LocalTime,
        r2End: LocalTime,
    ): AllowWindowsState {
        val windows = mutableListOf<AllowWindow>()
        if (r1Enabled) {
            windows += AllowWindow(start = r1Start, end = r1End, note = "")
        }
        if (r2Enabled) {
            val coveredByR1 = r1Enabled && TimeWindows.covers(r1Start, r1End, r2Start, r2End)
            if (!coveredByR1) {
                windows += AllowWindow(start = r2Start, end = r2End, note = "")
            }
        }
        return if (windows.isEmpty()) {
            AllowWindowsState(enabled = false, windows = listOf(NewDefaultWindow))
        } else {
            AllowWindowsState(enabled = true, windows = windows.take(AllowWindowFormat.MAX_WINDOWS))
        }
    }

    internal fun minuteOfDayToTime(minutes: Int?, fallback: LocalTime): LocalTime {
        if (minutes == null) return fallback
        val safe = minutes.coerceIn(0, 24 * 60 - 1)
        return LocalTime.of(safe / 60, safe % 60)
    }
}

object AllowWindowFormat {
    const val MAX_WINDOWS = 8
    const val MAX_NOTE_LENGTH = 20

    fun encode(windows: List<AllowWindow>): String =
        windows.take(MAX_WINDOWS).joinToString("\n") { window ->
            val note = window.note.trim().take(MAX_NOTE_LENGTH).replace("\n", " ")
            "${window.start.hour * 60 + window.start.minute},${window.end.hour * 60 + window.end.minute},$note"
        }

    fun decode(raw: String): List<AllowWindow> {
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence()
            .mapNotNull { line -> parseLine(line) }
            .take(MAX_WINDOWS)
            .toList()
    }

    fun sanitizeNote(note: String): String = note.trim().take(MAX_NOTE_LENGTH).replace("\n", " ")

    private fun parseLine(line: String): AllowWindow? {
        val first = line.indexOf(',')
        if (first <= 0) return null
        val second = line.indexOf(',', first + 1)
        if (second < 0) return null
        val startMin = line.substring(0, first).toIntOrNull() ?: return null
        val endMin = line.substring(first + 1, second).toIntOrNull() ?: return null
        val note = sanitizeNote(line.substring(second + 1))
        return AllowWindow(
            start = AllowWindowsMigration.minuteOfDayToTime(startMin, LocalTime.MIDNIGHT),
            end = AllowWindowsMigration.minuteOfDayToTime(endMin, LocalTime.MIDNIGHT),
            note = note,
        )
    }
}
