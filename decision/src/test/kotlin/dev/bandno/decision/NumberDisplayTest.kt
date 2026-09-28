package dev.bandno.decision

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberDisplayTest {
    @Test
    fun masksChineseMobileKeepingPrefixAndLastFour() {
        assertEquals("138****8000", NumberDisplay.mask("13800138000"))
    }

    @Test
    fun shortNumbersCollapseToStars() {
        assertEquals("****", NumberDisplay.mask("1234"))
        assertEquals("1****67", NumberDisplay.mask("1234567"))
        assertEquals("", NumberDisplay.mask(null))
        assertEquals("", NumberDisplay.mask(" "))
    }
}
