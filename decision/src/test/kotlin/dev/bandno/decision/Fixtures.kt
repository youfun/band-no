package dev.bandno.decision

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

internal val Shanghai: ZoneId = ZoneId.of("Asia/Shanghai")
internal val DefaultDay: LocalDate = LocalDate.of(2026, 4, 15)
internal val StrangerNumber = "13900139000"

internal fun at(
    hour: Int,
    minute: Int,
    isContact: Boolean = false,
    isPrivate: Boolean = false,
    priors: List<PriorAttempt> = emptyList(),
    date: LocalDate = DefaultDay,
    zoneId: ZoneId = Shanghai,
    normalizedNumber: String? = if (isPrivate) null else StrangerNumber,
): IncomingCall {
    val instant = LocalDateTime.of(date, LocalTime.of(hour, minute)).atZone(zoneId).toInstant()
    return IncomingCall(
        now = instant,
        zoneId = zoneId,
        isPrivateOrUnknown = isPrivate,
        isContact = isContact,
        priorAttempts = priors,
        normalizedNumber = normalizedNumber,
    )
}

internal fun Instant.plusMinutes(minutes: Long): Instant = plusSeconds(minutes * 60)

internal fun prior(at: Instant, blocked: Boolean = true) = PriorAttempt(at = at, blocked = blocked)

internal fun eveningWindow(
    startHour: Int = 18,
    startMinute: Int = 0,
    endHour: Int = 22,
    endMinute: Int = 0,
    note: String = "下班",
) = AllowWindow(LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute), note)
