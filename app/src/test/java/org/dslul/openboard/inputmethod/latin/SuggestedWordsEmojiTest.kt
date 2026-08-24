// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.latin

import org.dslul.openboard.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.dslul.openboard.inputmethod.latin.common.containsEmoji
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestedWordsEmojiTest {

    @Test
    fun testSuggestedWordInfoIsEmoji() {
        val emojiInfo = SuggestedWordInfo("😀", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue("Single emoji should be recognized by isEmoji", emojiInfo.isEmoji)

        val catEmojiInfo = SuggestedWordInfo("🐱", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue("Single cat emoji should be recognized by isEmoji", catEmojiInfo.isEmoji)

        val skinToneEmojiInfo = SuggestedWordInfo("👍🏽", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue("Modifier emoji sequence should be recognized by isEmoji", skinToneEmojiInfo.isEmoji)

        val zwjEmojiInfo = SuggestedWordInfo("👨‍👩‍👧‍👦", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue("ZWJ emoji sequence should be recognized by isEmoji", zwjEmojiInfo.isEmoji)

        val multiEmojiInfo = SuggestedWordInfo("😀😀", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertFalse("Multiple emojis concatenated is not a single emoji", multiEmojiInfo.isEmoji)

        val mixedEmojiInfo = SuggestedWordInfo("cat 🐱", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertFalse("Mixed text and emoji is not a single emoji", mixedEmojiInfo.isEmoji)

        val textInfo = SuggestedWordInfo("hello", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertFalse("Plain text is not an emoji", textInfo.isEmoji)
    }

    @Test
    fun testSuggestedWordsCandidateWithEmojiFiltered() {
        val typedWordInfo = SuggestedWordInfo("helo", "", SuggestedWordInfo.MAX_SCORE,
            SuggestedWordInfo.KIND_TYPED, null, -1, -1)
        val correctionInfo = SuggestedWordInfo("hello", "", 150,
            SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)

        val wordsList = arrayListOf(typedWordInfo, correctionInfo)
        val suggestedWords = SuggestedWords(
            wordsList,
            null,
            typedWordInfo,
            false /* typedWordValid */,
            true /* willAutoCorrect */,
            false /* isObsoleteSuggestions */,
            SuggestedWords.INPUT_STYLE_TYPING,
            0
        )

        assertEquals(2, suggestedWords.size())
        assertEquals("helo", suggestedWords.getWord(0))
        assertEquals("hello", suggestedWords.getWord(1))
        assertTrue(suggestedWords.mWillAutoCorrect)
        assertFalse(suggestedWords.getInfo(0).isEmoji)
        assertFalse(suggestedWords.getInfo(1).isEmoji)
    }

    @Test
    fun testContainsEmojiVsIsEmoji() {
        val mixedWord = "cat 🐱"
        val mixedInfo = SuggestedWordInfo(mixedWord, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertFalse(mixedInfo.isEmoji)
        assertTrue(containsEmoji(mixedWord))

        val singleEmoji = "🎉"
        val singleInfo = SuggestedWordInfo(singleEmoji, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue(singleInfo.isEmoji)
        assertTrue(containsEmoji(singleEmoji))

        val plainText = "hello"
        val textInfo = SuggestedWordInfo(plainText, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertFalse(textInfo.isEmoji)
        assertFalse(containsEmoji(plainText))
    }
}
