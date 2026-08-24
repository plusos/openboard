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

    @Test
    fun testContainsEmoji() {
        // Single emojis
        assertTrue(containsEmoji("😀"))
        assertTrue(containsEmoji("🎉"))
        assertTrue(containsEmoji("❤️"))
        assertTrue(containsEmoji("👍🏽"))
        assertTrue(containsEmoji("👨‍👩‍👧‍👦"))
        assertTrue(containsEmoji("0️⃣"))

        // Multiple emojis
        assertTrue(containsEmoji("😀😀"))
        assertTrue(containsEmoji("🎉🚀✨"))
        assertTrue(containsEmoji("❤️🔥"))

        // Mixed text and emojis
        assertTrue(containsEmoji("cat 🐱"))
        assertTrue(containsEmoji("party 🎉 time"))
        assertTrue(containsEmoji("hello 😀"))
        assertTrue(containsEmoji("🎉hello"))

        // Non-emojis (Latin, numbers, punctuation)
        assertFalse(containsEmoji("a"))
        assertFalse(containsEmoji("Z"))
        assertFalse(containsEmoji("hello"))
        assertFalse(containsEmoji("happy"))
        assertFalse(containsEmoji("123"))
        assertFalse(containsEmoji("!@#$%^&*()"))
        assertFalse(containsEmoji(null))
        assertFalse(containsEmoji(""))
        assertFalse(containsEmoji("   "))

        // Non-emojis in other scripts (Japanese, Cyrillic, Greek, Arabic)
        assertFalse(containsEmoji("こんにちは"))
        assertFalse(containsEmoji("日本語"))
        assertFalse(containsEmoji("Привет мир"))
        assertFalse(containsEmoji("Γειά σου"))
        assertFalse(containsEmoji("مرحبا بالعالم"))
    }
}

