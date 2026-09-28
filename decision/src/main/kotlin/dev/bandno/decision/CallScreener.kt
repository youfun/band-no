package dev.bandno.decision

import java.time.Duration

/**
 * Pure incoming-call policy. Allow-first, fail-open at the caller:
 *
 * 1. Hidden / empty number with [PrivateNumberPolicy.ALLOW]
 * 2. System contact when "always allow contacts" is on
 * 3. Normalized number matches a blocked prefix
 * 4. Any enabled allow-time window
 * 5. Repeat call within N minutes (R3)
 * 6. Default intercept ([ScreenSettings.blockAction])
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

        if (!call.isPrivateOrUnknown &&
            BlockedPrefixes.matches(call.normalizedNumber, settings.blockedPrefixes)
        ) {
            return ScreenDecision(blockActionOf(settings), RuleHit.PREFIX_BLOCK)
        }

        if (settings.allowWindowsEnabled &&
            settings.allowWindows.any { window ->
                TimeWindows.contains(localTime, window.start, window.end)
            }
        ) {
            return allow(RuleHit.ALLOW_WINDOW)
        }

        if (settings.r3Enabled && matchesRepeatCall(call, settings)) {
            return allow(RuleHit.R3_REPEAT_CALL)
        }

        return ScreenDecision(blockActionOf(settings), RuleHit.DEFAULT_BLOCK)
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
