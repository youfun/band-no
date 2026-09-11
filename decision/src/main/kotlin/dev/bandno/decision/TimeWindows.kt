package dev.bandno.decision

import java.time.LocalTime

/**
 * Half-open local-time windows: `[start, end)`.
 *
 * When `start == end`, the window is treated as 24 hours.
 * When `start > end`, the window crosses midnight (`[start, 24h) ∪ [00:00, end)`).
 */
object TimeWindows {
    fun contains(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        if (start == end) return true
        return if (start < end) {
            now >= start && now < end
        } else {
            now >= start || now < end
        }
    }

    /** True when every instant in `[innerStart, innerEnd)` also lies in the outer window. */
    fun covers(
        outerStart: LocalTime,
        outerEnd: LocalTime,
        innerStart: LocalTime,
        innerEnd: LocalTime,
    ): Boolean {
        if (innerStart == innerEnd) return outerStart == outerEnd
        return minutesIn(innerStart, innerEnd).all { minute ->
            contains(LocalTime.of(minute / 60, minute % 60), outerStart, outerEnd)
        }
    }

    private fun minutesIn(start: LocalTime, end: LocalTime): List<Int> {
        val startMin = start.hour * 60 + start.minute
        val endMin = end.hour * 60 + end.minute
        return when {
            startMin == endMin -> (0 until 24 * 60).toList()
            startMin < endMin -> (startMin until endMin).toList()
            else -> (startMin until 24 * 60).toList() + (0 until endMin).toList()
        }
    }
}
