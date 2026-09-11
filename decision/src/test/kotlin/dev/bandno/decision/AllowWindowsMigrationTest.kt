package dev.bandno.decision

import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AllowWindowsMigrationTest {
    @Test
    fun noKeysUsesNewEveningDefault() {
        val state = AllowWindowsMigration.resolve(
            encodedWindows = null,
            windowsEnabled = null,
            hasLegacyKeys = false,
            r1Enabled = null,
            r1StartMin = null,
            r1EndMin = null,
            r2Enabled = null,
            r2StartMin = null,
            r2EndMin = null,
        )
        assertTrue(state.enabled)
        assertEquals(listOf(AllowWindowsMigration.NewDefaultWindow), state.windows)
        assertEquals(LocalTime.of(18, 0), state.windows.single().start)
        assertEquals(LocalTime.of(22, 0), state.windows.single().end)
        assertEquals("下班", state.windows.single().note)
    }

    @Test
    fun encodedWindowsWinOverLegacyKeys() {
        val encoded = AllowWindowFormat.encode(
            listOf(AllowWindow(LocalTime.of(12, 0), LocalTime.of(13, 0), "自订")),
        )
        val state = AllowWindowsMigration.resolve(
            encodedWindows = encoded,
            windowsEnabled = false,
            hasLegacyKeys = true,
            r1Enabled = true,
            r1StartMin = 18 * 60,
            r1EndMin = 9 * 60,
            r2Enabled = true,
            r2StartMin = 19 * 60,
            r2EndMin = 20 * 60,
        )
        assertFalse(state.enabled)
        assertEquals("自订", state.windows.single().note)
        assertEquals(LocalTime.of(12, 0), state.windows.single().start)
    }

    @Test
    fun defaultLegacyR1AndR2CollapseToOvernightR1() {
        val state = AllowWindowsMigration.fromLegacy(
            r1Enabled = true,
            r1Start = LocalTime.of(18, 0),
            r1End = LocalTime.of(9, 0),
            r2Enabled = true,
            r2Start = LocalTime.of(19, 0),
            r2End = LocalTime.of(20, 0),
        )
        assertTrue(state.enabled)
        assertEquals(1, state.windows.size)
        assertEquals(LocalTime.of(18, 0), state.windows.single().start)
        assertEquals(LocalTime.of(9, 0), state.windows.single().end)
        assertEquals("", state.windows.single().note)
    }

    @Test
    fun customR2NotCoveredByR1IsKept() {
        val state = AllowWindowsMigration.fromLegacy(
            r1Enabled = true,
            r1Start = LocalTime.of(18, 0),
            r1End = LocalTime.of(9, 0),
            r2Enabled = true,
            r2Start = LocalTime.of(12, 0),
            r2End = LocalTime.of(13, 0),
        )
        assertEquals(2, state.windows.size)
        assertEquals(LocalTime.of(18, 0), state.windows[0].start)
        assertEquals(LocalTime.of(12, 0), state.windows[1].start)
        assertEquals(LocalTime.of(13, 0), state.windows[1].end)
    }

    @Test
    fun shortenedR1LeavesDefaultR2Uncovered() {
        val state = AllowWindowsMigration.fromLegacy(
            r1Enabled = true,
            r1Start = LocalTime.of(22, 0),
            r1End = LocalTime.of(8, 0),
            r2Enabled = true,
            r2Start = LocalTime.of(19, 0),
            r2End = LocalTime.of(20, 0),
        )
        assertEquals(2, state.windows.size)
        assertEquals(LocalTime.of(22, 0), state.windows[0].start)
        assertEquals(LocalTime.of(19, 0), state.windows[1].start)
    }

    @Test
    fun r1DisabledKeepsEnabledR2() {
        val state = AllowWindowsMigration.fromLegacy(
            r1Enabled = false,
            r1Start = LocalTime.of(18, 0),
            r1End = LocalTime.of(9, 0),
            r2Enabled = true,
            r2Start = LocalTime.of(19, 0),
            r2End = LocalTime.of(20, 0),
        )
        assertTrue(state.enabled)
        assertEquals(1, state.windows.size)
        assertEquals(LocalTime.of(19, 0), state.windows.single().start)
        assertEquals(LocalTime.of(20, 0), state.windows.single().end)
    }

    @Test
    fun bothLegacyRulesDisabledUsesNewDefaultOff() {
        val state = AllowWindowsMigration.fromLegacy(
            r1Enabled = false,
            r1Start = LocalTime.of(18, 0),
            r1End = LocalTime.of(9, 0),
            r2Enabled = false,
            r2Start = LocalTime.of(19, 0),
            r2End = LocalTime.of(20, 0),
        )
        assertFalse(state.enabled)
        assertEquals(listOf(AllowWindowsMigration.NewDefaultWindow), state.windows)
    }

    @Test
    fun resolveWithLegacyKeysUsesOldDefaultsForMissingMinutes() {
        val state = AllowWindowsMigration.resolve(
            encodedWindows = null,
            windowsEnabled = null,
            hasLegacyKeys = true,
            r1Enabled = true,
            r1StartMin = null,
            r1EndMin = null,
            r2Enabled = true,
            r2StartMin = null,
            r2EndMin = null,
        )
        assertEquals(1, state.windows.size)
        assertEquals(LocalTime.of(18, 0), state.windows.single().start)
        assertEquals(LocalTime.of(9, 0), state.windows.single().end)
    }

    @Test
    fun encodeDecodeRoundTripKeepsNote() {
        val windows = listOf(
            AllowWindow(LocalTime.of(18, 0), LocalTime.of(22, 0), "下班"),
            AllowWindow(LocalTime.of(12, 0), LocalTime.of(13, 0), ""),
        )
        val decoded = AllowWindowFormat.decode(AllowWindowFormat.encode(windows))
        assertEquals(windows, decoded)
    }
}
