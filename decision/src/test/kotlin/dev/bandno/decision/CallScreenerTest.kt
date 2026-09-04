package dev.bandno.decision

import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CallScreenerTest {
    private val defaults = ScreenSettings.Default

    @Test
    fun strangerAtTenIsSilenced() {
        val decision = CallScreener.decide(at(10, 0), defaults)
        assertEquals(DecisionAction.SILENCE, decision.action)
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun strangerAtNineteenThirtyRingsViaR2() {
        val decision = CallScreener.decide(at(19, 30), defaults)
        assertEquals(DecisionAction.ALLOW, decision.action)
        assertEquals(RuleHit.R2_IMPORTANT_WINDOW, decision.ruleHit)
    }

    @Test
    fun updatedR1WindowAppliesWithoutReset() {
        val settings = defaults.copy(
            r1Start = LocalTime.of(20, 0),
            r1End = LocalTime.of(8, 0),
            r2Enabled = false,
        )
        assertEquals(RuleHit.R1_ALLOW_WINDOW, CallScreener.decide(at(21, 0), settings).ruleHit)
        assertEquals(RuleHit.R1_ALLOW_WINDOW, CallScreener.decide(at(7, 30), settings).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(10, 0), settings).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(19, 30), settings).ruleHit)
    }

    @Test
    fun secondCallWithinThreeMinutesRingsViaR3() {
        val first = at(14, 0)
        val second = first.copy(
            now = first.now.plusMinutes(2),
            priorAttempts = listOf(prior(first.now, blocked = true)),
        )
        val firstDecision = CallScreener.decide(first, defaults)
        val secondDecision = CallScreener.decide(second, defaults)
        assertEquals(DecisionAction.SILENCE, firstDecision.action)
        assertEquals(RuleHit.R3_REPEAT_CALL, secondDecision.ruleHit)
        assertEquals(DecisionAction.ALLOW, secondDecision.action)
    }

    @Test
    fun secondCallAfterIntervalRemainsBlocked() {
        val first = at(14, 0)
        val second = first.copy(
            now = first.now.plusMinutes(3),
            priorAttempts = listOf(prior(first.now, blocked = true)),
        )
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(second, defaults).ruleHit)
    }

    @Test
    fun contactAlwaysRingsWhenEnabled() {
        val decision = CallScreener.decide(at(10, 0, isContact = true), defaults)
        assertEquals(RuleHit.CONTACT, decision.ruleHit)
        assertEquals(DecisionAction.ALLOW, decision.action)
    }

    @Test
    fun disablingR2DropsImportantWindowPrivilegeButR1MayStillAllow() {
        val r2Off = defaults.copy(r2Enabled = false)
        val stillR1 = CallScreener.decide(at(19, 30), r2Off)
        assertEquals(RuleHit.R1_ALLOW_WINDOW, stillR1.ruleHit)

        val r1DoesNotCoverEvening = r2Off.copy(
            r1Start = LocalTime.of(22, 0),
            r1End = LocalTime.of(8, 0),
        )
        val blocked = CallScreener.decide(at(19, 30), r1DoesNotCoverEvening)
        assertEquals(RuleHit.DEFAULT_BLOCK, blocked.ruleHit)
    }

    @Test
    fun overnightR1AllowsLateNightAndEarlyMorning() {
        assertEquals(RuleHit.R1_ALLOW_WINDOW, CallScreener.decide(at(18, 0), defaults).ruleHit)
        assertEquals(RuleHit.R1_ALLOW_WINDOW, CallScreener.decide(at(23, 0), defaults).ruleHit)
        assertEquals(RuleHit.R1_ALLOW_WINDOW, CallScreener.decide(at(0, 5), defaults).ruleHit)
        assertEquals(RuleHit.R1_ALLOW_WINDOW, CallScreener.decide(at(8, 59), defaults).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(9, 0), defaults).ruleHit)
    }

    @Test
    fun r2BeatsDefaultBlockInsideWindowEvenIfR1Off() {
        val settings = defaults.copy(r1Enabled = false)
        assertEquals(RuleHit.R2_IMPORTANT_WINDOW, CallScreener.decide(at(19, 0), settings).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(20, 0), settings).ruleHit)
    }

    @Test
    fun privateNumberDefaultsToAllow() {
        val decision = CallScreener.decide(at(10, 0, isPrivate = true), defaults)
        assertEquals(RuleHit.PRIVATE_NUMBER, decision.ruleHit)
        assertEquals(DecisionAction.ALLOW, decision.action)
    }

    @Test
    fun privateNumberFollowsRulesWhenConfigured() {
        val settings = defaults.copy(privateNumberPolicy = PrivateNumberPolicy.FOLLOW_RULES)
        val decision = CallScreener.decide(at(10, 0, isPrivate = true), settings)
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun r3CanRequireTheFirstAttemptWasBlocked() {
        val settings = defaults.copy(r3RequireFirstBlocked = true)
        val first = at(14, 0)
        val allowedPrior = first.copy(
            now = first.now.plusMinutes(1),
            priorAttempts = listOf(prior(first.now, blocked = false)),
        )
        val blockedPrior = first.copy(
            now = first.now.plusMinutes(1),
            priorAttempts = listOf(prior(first.now, blocked = true)),
        )
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(allowedPrior, settings).ruleHit)
        assertEquals(RuleHit.R3_REPEAT_CALL, CallScreener.decide(blockedPrior, settings).ruleHit)
    }

    @Test
    fun r1StrangersOnlySkipsContactsWhenContactsAreNotAlwaysAllowed() {
        val settings = defaults.copy(
            alwaysAllowContacts = false,
            r1StrangersOnly = true,
            r2Enabled = false,
        )
        val contact = CallScreener.decide(at(18, 30, isContact = true), settings)
        val stranger = CallScreener.decide(at(18, 30), settings)
        assertEquals(RuleHit.DEFAULT_BLOCK, contact.ruleHit)
        assertEquals(RuleHit.R1_ALLOW_WINDOW, stranger.ruleHit)
    }

    @Test
    fun rejectActionIsUsedWhenConfigured() {
        val settings = defaults.copy(blockAction = BlockAction.REJECT)
        val decision = CallScreener.decide(at(10, 0), settings)
        assertEquals(DecisionAction.REJECT, decision.action)
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun allowWinsWhenWindowsOverlap() {
        val decision = CallScreener.decide(at(19, 15), defaults)
        assertEquals(DecisionAction.ALLOW, decision.action)
        assertEquals(RuleHit.R2_IMPORTANT_WINDOW, decision.ruleHit)
    }
}
