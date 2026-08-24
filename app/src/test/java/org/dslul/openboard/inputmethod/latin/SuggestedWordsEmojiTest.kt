// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.latin

import org.dslul.openboard.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.dslul.openboard.inputmethod.latin.common.containsEmoji
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayList

class SuggestedWordsEmojiTest {

    @Test
    fun testSuggestedWordInfoIsEmoji() {
        val emojiInfo = SuggestedWordInfo("😀", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue(emojiInfo.isEmoji)

        val multiEmojiInfo = SuggestedWordInfo("😀😀", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue(multiEmojiInfo.isEmoji)

        val mixedEmojiInfo = SuggestedWordInfo("cat 🐱", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertTrue(mixedEmojiInfo.isEmoji)

        val textInfo = SuggestedWordInfo("hello", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1)
        assertFalse(textInfo.isEmoji)
    }

    @Test
    fun testFilterEmojiSuggestions() {
        val list = ArrayList<SuggestedWordInfo>().apply {
            add(SuggestedWordInfo("hello", "", 100, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1))
            add(SuggestedWordInfo("😀", "", 90, SuggestedWordInfo.KIND_SHORTCUT, null, -1, -1))
            add(SuggestedWordInfo("world", "", 80, SuggestedWordInfo.KIND_CORRECTION, null, -1, -1))
            add(SuggestedWordInfo("🎉✨", "", 70, SuggestedWordInfo.KIND_SHORTCUT, null, -1, -1))
            add(SuggestedWordInfo("cat 🐱", "", 60, SuggestedWordInfo.KIND_SHORTCUT, null, -1, -1))
        }

        for (i in list.size - 1 downTo 0) {
            if (containsEmoji(list[i].mWord)) {
                list.removeAt(i)
            }
        }

        assertEquals(2, list.size)
        assertEquals("hello", list[0].mWord)
        assertEquals("world", list[1].mWord)
    }
}
