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
}
