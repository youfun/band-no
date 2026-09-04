package dev.bandno.decision

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

internal val Shanghai: ZoneId = ZoneId.of("Asia/Shanghai")
internal val DefaultDay: LocalDate = LocalDate.of(2026, 4, 15)

internal fun at(
    hour: Int,
    minute: Int,
    isContact: Boolean = false,
    isPrivate: Boolean = false,
    priors: List<PriorAttempt> = emptyList(),
    date: LocalDate = DefaultDay,
    zoneId: ZoneId = Shanghai,
): IncomingCall {
    val instant = LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zoneId).toInstant()
    return IncomingCall(
        now = instant,
        zoneId = zoneId,
        isPrivateOrUnknown = isPrivate,
        isContact = isContact,
        priorAttempts = priors,
    )
}

internal fun Instant.plusMinutes(minutes: Long): Instant = plusSeconds(minutes * 60)

internal fun prior(at: Instant, blocked: Boolean = true) = PriorAttempt(at = at, blocked = blocked)
