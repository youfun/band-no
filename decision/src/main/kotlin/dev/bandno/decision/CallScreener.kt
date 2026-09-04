package dev.bandno.decision

import java.time.Duration

/**
 * Pure incoming-call policy. Order is allow-first (see product spec §4):
 *
 * 1. Hidden / empty number with [PrivateNumberPolicy.ALLOW]
 * 2. System contact when "always allow contacts" is on
 * 3. R2 important window
 * 4. R1 allow window
 * 5. R3 repeat call within N minutes
 * 6. Default intercept ([ScreenSettings.blockAction])
 *
 * Fail-open is the caller's job: this function itself always returns a decision.
 */
object CallScreener {
    fun decide(call: IncomingCall, settings: ScreenSettings): ScreenDecision {
        val localTime = call.now.atZone(call.zoneId).toLocalTime()

        if (call.isPrivateOrUnknown && settings.privateNumberPolicy == PrivateNumberPolicy.ALLOW) {
            return allow(RuleHit.PRIVATE_NUMBER)
        }

        if (call.isContact && settings.alwaysAllowContacts) {
            return allow(RuleHit.CONTACT)
        }

        if (settings.r2Enabled &&
            TimeWindows.contains(localTime, settings.r2Start, settings.r2End)
        ) {
            return allow(RuleHit.R2_IMPORTANT_WINDOW)
        }

        if (settings.r1Enabled &&
            TimeWindows.contains(localTime, settings.r1Start, settings.r1End) &&
            r1Applies(call, settings)
        ) {
            return allow(RuleHit.R1_ALLOW_WINDOW)
        }

        if (settings.r3Enabled && matchesRepeatCall(call, settings)) {
            return allow(RuleHit.R3_REPEAT_CALL)
        }

        return ScreenDecision(blockActionOf(settings), RuleHit.DEFAULT_BLOCK)
    }

    private fun r1Applies(call: IncomingCall, settings: ScreenSettings): Boolean {
        if (!settings.r1StrangersOnly) return true
        return !call.isContact
    }

    private fun matchesRepeatCall(call: IncomingCall, settings: ScreenSettings): Boolean {
        val window = Duration.ofMinutes(settings.r3IntervalMinutes.toLong())
        val inWindow = call.priorAttempts.filter { attempt ->
            val age = Duration.between(attempt.at, call.now)
            !age.isNegative && age < window
        }
        if (inWindow.isEmpty()) return false
        return if (settings.r3RequireFirstBlocked) inWindow.any { it.blocked } else true
    }

    private fun allow(hit: RuleHit) = ScreenDecision(DecisionAction.ALLOW, hit)

    private fun blockActionOf(settings: ScreenSettings): DecisionAction =
        when (settings.blockAction) {
            BlockAction.SILENCE -> DecisionAction.SILENCE
            BlockAction.REJECT -> DecisionAction.REJECT
        }
}
