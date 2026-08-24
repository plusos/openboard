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

    @Test
    fun testKeyDrawParamsSelectTextColor() {
        val params = org.dslul.openboard.inputmethod.keyboard.internal.KeyDrawParams()
        params.mTextColor = 0xFFFFFFFF.toInt()
        val key = org.dslul.openboard.inputmethod.keyboard.Key(
            "😀",
            org.dslul.openboard.inputmethod.keyboard.internal.KeyboardIconsSet.ICON_UNDEFINED,
            org.dslul.openboard.inputmethod.latin.common.Constants.CODE_OUTPUT_TEXT,
            "😀",
            null,
            org.dslul.openboard.inputmethod.keyboard.Key.LABEL_FLAGS_FONT_NORMAL,
            org.dslul.openboard.inputmethod.keyboard.Key.BACKGROUND_TYPE_EMPTY,
            0, 0, 50, 50, 0, 0
        )
        val color = key.selectTextColor(params)
        org.junit.Assert.assertEquals(0xFFFFFFFF.toInt(), color)
    }
}
