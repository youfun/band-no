package dev.bandno.decision

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NumberNormalizerTest {
    @Test
    fun stripsSeparatorsAndChinaCountryCode() {
        val expected = "13800138000"
        assertEquals(expected, NumberNormalizer.normalize("+86 138-0013-8000"))
        assertEquals(expected, NumberNormalizer.normalize("0086 13800138000"))
        assertEquals(expected, NumberNormalizer.normalize("8613800138000"))
        assertEquals(expected, NumberNormalizer.normalize("13800138000"))
        assertEquals(expected, NumberNormalizer.normalize("013800138000"))
    }

    @Test
    fun blankOrNonDigitIsNull() {
        assertNull(NumberNormalizer.normalize(null))
        assertNull(NumberNormalizer.normalize("  "))
        assertNull(NumberNormalizer.normalize("私人号码"))
    }

    @Test
    fun keepsDomesticLandlineDigits() {
        assertEquals("01012345678", NumberNormalizer.normalize("010-1234-5678"))
    }
}
