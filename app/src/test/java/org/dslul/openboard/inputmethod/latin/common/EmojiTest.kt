// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.latin.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiTest {

    @Test
    fun testBasicEmojis() {
        assertTrue(isEmoji("😀"))
        assertTrue(isEmoji("🎉"))
        assertTrue(isEmoji("❤️"))
        assertTrue(isEmoji("🔥"))
        assertTrue(isEmoji("🚀"))
        assertTrue(isEmoji("✨"))
    }

    @Test
    fun testSpecialSymbolEmojis() {
        assertTrue("Copyright symbol should be recognized as emoji", isEmoji("©"))
        assertTrue("Registered symbol should be recognized as emoji", isEmoji("®"))
        assertTrue("Wavy dash should be recognized as emoji", isEmoji("〰"))
        assertTrue("Part alternation mark should be recognized as emoji", isEmoji("〽"))
        assertTrue("Circled ideograph congratulation should be recognized as emoji", isEmoji("㊗"))
        assertTrue("Circled ideograph secret should be recognized as emoji", isEmoji("㊙"))
    }

    @Test
    fun testComplexSequenceEmojis() {
        // Skin tone modifiers
        assertTrue(isEmoji("👍🏽"))
        assertTrue(isEmoji("👋🏿"))

        // ZWJ Sequences
        assertTrue(isEmoji("👨‍👩‍👧‍👦"))
        assertTrue(isEmoji("👩‍💻"))
        assertTrue(isEmoji("🏳️‍🌈"))
        assertTrue(isEmoji("🐈‍⬛"))

        // Flag sequences
        assertTrue(isEmoji("🇺🇸"))
        assertTrue(isEmoji("🇨🇦"))
        assertTrue(isEmoji("🏴󠁧󠁢󠁳󠁣󠁴󠁿"))

        // Keycap emojis
        assertTrue(isEmoji("0️⃣"))
        assertTrue(isEmoji("#️⃣"))
    }

    @Test
    fun testNonEmojis() {
        assertFalse(isEmoji("a"))
        assertFalse(isEmoji("Z"))
        assertFalse(isEmoji("1"))
        assertFalse(isEmoji("hello"))
        assertFalse(isEmoji("!"))
        assertFalse(isEmoji(""))
        assertFalse(isEmoji("   "))
        assertFalse(isEmoji("😀😀")) // Multi-emoji string is not single emoji
        assertFalse(isEmoji("cat 😀"))
    }

    @Test
    fun testSplitOnWhitespace() {
        assertEquals(listOf("hello", "world"), "hello world".splitOnWhitespace())
        assertEquals(listOf("red", "heart"), "  red   heart  ".splitOnWhitespace())
        assertEquals(emptyList<String>(), "   ".splitOnWhitespace())
    }
}
