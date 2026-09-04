package dev.bandno.decision

import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeWindowsTest {
    @Test
    fun sameDayWindowIsStartInclusiveEndExclusive() {
        val start = LocalTime.of(19, 0)
        val end = LocalTime.of(20, 0)
        assertTrue(TimeWindows.contains(LocalTime.of(19, 0), start, end))
        assertTrue(TimeWindows.contains(LocalTime.of(19, 59, 59), start, end))
        assertFalse(TimeWindows.contains(LocalTime.of(20, 0), start, end))
        assertFalse(TimeWindows.contains(LocalTime.of(18, 59), start, end))
    }

    @Test
    fun overnightWindowCoversEveningAndMorning() {
        val start = LocalTime.of(18, 0)
        val end = LocalTime.of(9, 0)
        assertTrue(TimeWindows.contains(LocalTime.of(18, 0), start, end))
        assertTrue(TimeWindows.contains(LocalTime.of(23, 59), start, end))
        assertTrue(TimeWindows.contains(LocalTime.of(0, 0), start, end))
        assertTrue(TimeWindows.contains(LocalTime.of(8, 59), start, end))
        assertFalse(TimeWindows.contains(LocalTime.of(9, 0), start, end))
        assertFalse(TimeWindows.contains(LocalTime.of(10, 0), start, end))
        assertFalse(TimeWindows.contains(LocalTime.of(17, 59), start, end))
    }

    @Test
    fun equalStartAndEndMeansTwentyFourHours() {
        val noon = LocalTime.of(12, 0)
        assertTrue(TimeWindows.contains(LocalTime.of(0, 0), noon, noon))
        assertTrue(TimeWindows.contains(LocalTime.of(12, 0), noon, noon))
        assertTrue(TimeWindows.contains(LocalTime.of(23, 59), noon, noon))
    }
}
