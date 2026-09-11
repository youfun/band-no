package dev.bandno.decision

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BlockedPrefixesTest {
    @Test
    fun sanitizeRejectsTwoDigitsAndAcceptsThreeToSeven() {
        assertNull(BlockedPrefixes.sanitize("17"))
        assertEquals("170", BlockedPrefixes.sanitize("170"))
        assertEquals("1701234", BlockedPrefixes.sanitize("170-1234"))
        assertNull(BlockedPrefixes.sanitize("17012345"))
    }

    @Test
    fun addIgnoresDuplicatesAndCapsAtThirty() {
        val once = BlockedPrefixes.add(emptyList(), "170")
        assertEquals(listOf("170"), BlockedPrefixes.add(once, "170"))
        val full = (1..30).map { index -> (100 + index).toString() }
        assertEquals(30, full.size)
        assertEquals(full, BlockedPrefixes.add(full, "199"))
    }

    @Test
    fun matchesRequiresNormalizedPrefix() {
        assertTrue(BlockedPrefixes.matches("17012345678", listOf("170")))
        assertFalse(BlockedPrefixes.matches("17012345678", emptyList()))
        assertFalse(BlockedPrefixes.matches(null, listOf("170")))
        assertFalse(BlockedPrefixes.matches("17012345678", listOf("17")))
    }
}
