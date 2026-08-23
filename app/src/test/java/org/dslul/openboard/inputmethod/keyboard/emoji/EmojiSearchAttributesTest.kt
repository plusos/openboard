// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.keyboard.emoji

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.dslul.openboard.inputmethod.latin.InputAttributes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiSearchAttributesTest {

    @Test
    fun testIsEmojiSearch() {
        val editorInfo = EditorInfo()
        editorInfo.privateImeOptions = EmojiSearchActivity.PRIVATE_IME_OPTIONS_PREFIX + ",custom"
        assertTrue(EmojiSearchActivity.isEmojiSearch(editorInfo))

        editorInfo.privateImeOptions = "com.example.other"
        assertFalse(EmojiSearchActivity.isEmojiSearch(editorInfo))

        editorInfo.privateImeOptions = null
        assertFalse(EmojiSearchActivity.isEmojiSearch(editorInfo))
    }

    @Test
    fun testInputAttributesDisablesSuggestionsDuringEmojiSearch() {
        val emojiSearchEditorInfo = EditorInfo()
        emojiSearchEditorInfo.inputType = InputType.TYPE_CLASS_TEXT
        emojiSearchEditorInfo.privateImeOptions = EmojiSearchActivity.PRIVATE_IME_OPTIONS_PREFIX + ","

        val attributes = InputAttributes(emojiSearchEditorInfo, false, "org.dslul.openboard.inputmethod.latin")
        assertFalse(attributes.mShouldShowSuggestions)
        assertTrue(attributes.mInputTypeNoAutoCorrect)
    }

    @Test
    fun testInputAttributesEnablesSuggestionsForNormalText() {
        val normalEditorInfo = EditorInfo()
        normalEditorInfo.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT

        val attributes = InputAttributes(normalEditorInfo, false, "org.dslul.openboard.inputmethod.latin")
        assertTrue(attributes.mShouldShowSuggestions)
        assertFalse(attributes.mInputTypeNoAutoCorrect)
    }
}
