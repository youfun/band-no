package dev.bandno.decision

import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CallScreenerTest {
    private val defaults = ScreenSettings.Default

    @Test
    fun strangerInsideDefaultEveningWindowIsAllowed() {
        val decision = CallScreener.decide(at(18, 0), defaults)
        assertEquals(DecisionAction.ALLOW, decision.action)
        assertEquals(RuleHit.ALLOW_WINDOW, decision.ruleHit)
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(21, 59), defaults).ruleHit)
    }

    @Test
    fun strangerAtWindowEndIsBlocked() {
        val decision = CallScreener.decide(at(22, 0), defaults)
        assertEquals(DecisionAction.SILENCE, decision.action)
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun strangerInDaytimeIsBlocked() {
        val decision = CallScreener.decide(at(10, 0), defaults)
        assertEquals(DecisionAction.SILENCE, decision.action)
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun extraLunchWindowAlsoAllows() {
        val settings = defaults.copy(
            allowWindows = listOf(
                eveningWindow(),
                AllowWindow(LocalTime.of(12, 0), LocalTime.of(13, 0), note = ""),
            ),
        )
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(12, 0), settings).ruleHit)
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(12, 59), settings).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(13, 0), settings).ruleHit)
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(19, 0), settings).ruleHit)
    }

    @Test
    fun userAddedOvernightWindowStillWorks() {
        val settings = defaults.copy(
            allowWindows = listOf(
                AllowWindow(LocalTime.of(22, 0), LocalTime.of(8, 0), note = ""),
            ),
        )
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(22, 0), settings).ruleHit)
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(23, 30), settings).ruleHit)
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(0, 5), settings).ruleHit)
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(7, 59), settings).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(8, 0), settings).ruleHit)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(19, 0), settings).ruleHit)
    }

    @Test
    fun prefixBlockBeatsAllowWindowAndRepeatCall() {
        val first = at(19, 0, normalizedNumber = "17012345678")
        val second = first.copy(
            now = first.now.plusMinutes(2),
            priorAttempts = listOf(prior(first.now, blocked = true)),
        )
        val settings = defaults.copy(blockedPrefixes = listOf("170"))
        val firstDecision = CallScreener.decide(first, settings)
        val secondDecision = CallScreener.decide(second, settings)
        assertEquals(DecisionAction.SILENCE, firstDecision.action)
        assertEquals(RuleHit.PREFIX_BLOCK, firstDecision.ruleHit)
        assertEquals(RuleHit.PREFIX_BLOCK, secondDecision.ruleHit)
        assertEquals(DecisionAction.SILENCE, secondDecision.action)
    }

    @Test
    fun contactStillAllowedWhenPrefixWouldMatch() {
        val decision = CallScreener.decide(
            at(10, 0, isContact = true, normalizedNumber = "17012345678"),
            defaults.copy(blockedPrefixes = listOf("170")),
        )
        assertEquals(RuleHit.CONTACT, decision.ruleHit)
        assertEquals(DecisionAction.ALLOW, decision.action)
    }

    @Test
    fun privateNumberSkipsPrefixEvenWhenFollowRules() {
        val settings = defaults.copy(
            privateNumberPolicy = PrivateNumberPolicy.FOLLOW_RULES,
            blockedPrefixes = listOf("170"),
        )
        val decision = CallScreener.decide(
            at(10, 0, isPrivate = true, normalizedNumber = "17012345678"),
            settings,
        )
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun privateNumberDefaultsToAllow() {
        val decision = CallScreener.decide(at(10, 0, isPrivate = true), defaults)
        assertEquals(RuleHit.PRIVATE_NUMBER, decision.ruleHit)
        assertEquals(DecisionAction.ALLOW, decision.action)
    }

    @Test
    fun plus86NumberHitsPrefixAfterNormalize() {
        val normalized = NumberNormalizer.normalize("+86 170-1234-5678")
        assertEquals("17012345678", normalized)
        val decision = CallScreener.decide(
            at(19, 0, normalizedNumber = normalized),
            defaults.copy(blockedPrefixes = listOf("170")),
        )
        assertEquals(RuleHit.PREFIX_BLOCK, decision.ruleHit)
    }

    @Test
    fun twoDigitPrefixIsIgnored() {
        val settings = defaults.copy(blockedPrefixes = listOf("17"))
        assertEquals(RuleHit.ALLOW_WINDOW, CallScreener.decide(at(19, 0, normalizedNumber = "17012345678"), settings).ruleHit)
    }

    @Test
    fun prefixesFromThreeToSevenDigitsMatch() {
        val number = "16512345678"
        assertEquals(
            RuleHit.PREFIX_BLOCK,
            CallScreener.decide(at(10, 0, normalizedNumber = number), defaults.copy(blockedPrefixes = listOf("165"))).ruleHit,
        )
        assertEquals(
            RuleHit.PREFIX_BLOCK,
            CallScreener.decide(at(10, 0, normalizedNumber = number), defaults.copy(blockedPrefixes = listOf("1651234"))).ruleHit,
        )
        assertEquals(
            RuleHit.DEFAULT_BLOCK,
            CallScreener.decide(at(10, 0, normalizedNumber = number), defaults.copy(blockedPrefixes = listOf("16512345"))).ruleHit,
        )
    }

    @Test
    fun emptyPrefixListDoesNotBlock() {
        val settings = defaults.copy(blockedPrefixes = emptyList())
        assertEquals(
            RuleHit.DEFAULT_BLOCK,
            CallScreener.decide(at(10, 0, normalizedNumber = "17012345678"), settings).ruleHit,
        )
    }

    @Test
    fun secondCallWithinThreeMinutesRingsWhenPrefixMisses() {
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
    fun disabledAllowWindowsFallThroughToDefaultBlock() {
        val settings = defaults.copy(allowWindowsEnabled = false)
        assertEquals(RuleHit.DEFAULT_BLOCK, CallScreener.decide(at(19, 0), settings).ruleHit)
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
    fun rejectActionIsUsedWhenConfigured() {
        val settings = defaults.copy(blockAction = BlockAction.REJECT)
        val decision = CallScreener.decide(at(10, 0), settings)
        assertEquals(DecisionAction.REJECT, decision.action)
        assertEquals(RuleHit.DEFAULT_BLOCK, decision.ruleHit)
    }

    @Test
    fun prefixUsesGlobalRejectAction() {
        val settings = defaults.copy(
            blockAction = BlockAction.REJECT,
            blockedPrefixes = listOf("170"),
        )
        val decision = CallScreener.decide(at(19, 0, normalizedNumber = "17012345678"), settings)
        assertEquals(DecisionAction.REJECT, decision.action)
        assertEquals(RuleHit.PREFIX_BLOCK, decision.ruleHit)
    }
}
