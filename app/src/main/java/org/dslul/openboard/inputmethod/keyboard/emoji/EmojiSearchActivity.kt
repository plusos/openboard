// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package org.dslul.openboard.inputmethod.keyboard.emoji

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.dslul.openboard.inputmethod.keyboard.Key
import org.dslul.openboard.inputmethod.keyboard.Keyboard
import org.dslul.openboard.inputmethod.keyboard.KeyboardId
import org.dslul.openboard.inputmethod.keyboard.KeyboardLayoutSet
import org.dslul.openboard.inputmethod.keyboard.KeyboardSwitcher
import org.dslul.openboard.inputmethod.keyboard.KeyboardTheme
import org.dslul.openboard.inputmethod.keyboard.ProximityInfo
import org.dslul.openboard.inputmethod.keyboard.internal.KeyboardIconsSet
import org.dslul.openboard.inputmethod.latin.Dictionary
import org.dslul.openboard.inputmethod.latin.DictionaryFactory
import org.dslul.openboard.inputmethod.latin.LatinIME
import org.dslul.openboard.inputmethod.latin.R
import org.dslul.openboard.inputmethod.latin.RichInputMethodManager
import org.dslul.openboard.inputmethod.latin.RichInputMethodSubtype
import org.dslul.openboard.inputmethod.latin.SingleDictionaryFacilitator
import org.dslul.openboard.inputmethod.latin.common.Constants
import org.dslul.openboard.inputmethod.latin.common.splitOnWhitespace
import org.dslul.openboard.inputmethod.latin.settings.Settings
import org.dslul.openboard.inputmethod.latin.utils.DictionaryInfoUtils
import org.dslul.openboard.inputmethod.latin.utils.ResourceUtils

private const val TAG = "EmojiSearchActivity"

class EmojiSearchActivity : ComponentActivity() {
    private var firstSearchDone = false
    private var screenHeight: Int = 0
    private lateinit var hintLocales: LocaleList
    private lateinit var emojiPageKeyboardView: EmojiPageKeyboardView
    private lateinit var templateKeyboard: Keyboard
    private var templateKey0: Key? = null
    private var firstKey: Key? = null
    private var pressedKey: Key? = null
    private var currentSearchText: String = ""

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            currentSearchText = savedInstanceState.getString("SEARCH_TEXT", "")
        }
        init()
        enableEdgeToEdge()
        setContent {
            val theme = KeyboardTheme.getKeyboardTheme(this)
            val isSystemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val isDark = when (KeyboardTheme.getThemeVariant(theme.mThemeId)) {
                KeyboardTheme.THEME_VARIANT_LIGHT, KeyboardTheme.THEME_VARIANT_WHITE -> false
                KeyboardTheme.THEME_VARIANT_DARK -> true
                else -> isSystemDark
            }
            val surfaceBgColor = if (isDark) Color(0xFF212121) else Color(0xFFECEFF1)
            val fieldContainerColor = if (isDark) Color(0xFF333333) else Color(0xFFFFFFFF)
            val textColor = if (isDark) Color.White else Color(0xFF212121)
            val placeholderColor = if (isDark) Color(0xFFAAAAAA) else Color(0xFF757575)
            val iconTint = if (isDark) Color.White else Color(0xFF424242)

            LocalContext.current.setTheme(theme.mStyleId)
            BackHandler { cancel() }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { cancel() }
                    ),
                color = Color(0x80000000)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(surfaceBgColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { cancel() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = stringResource(R.string.spoken_description_action_previous),
                                    tint = iconTint
                                )
                            }
                            Text(
                                text = stringResource(R.string.emoji_search_title),
                                fontSize = 18.sp,
                                color = textColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically)
                            )
                        }
                        key(emojiPageKeyboardView) {
                            AndroidView(
                                factory = { emojiPageKeyboardView },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                            )
                        }
                        val focusRequester = remember { FocusRequester() }
                        val keyboardController = LocalSoftwareKeyboardController.current
                        var text by remember {
                            mutableStateOf(
                                TextFieldValue(
                                    currentSearchText,
                                    selection = TextRange(currentSearchText.length)
                                )
                            )
                        }
                        val textFieldColors = TextFieldDefaults.colors(
                            focusedContainerColor = fieldContainerColor,
                            unfocusedContainerColor = fieldContainerColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = textColor,
                            focusedLeadingIconColor = iconTint,
                            unfocusedLeadingIconColor = iconTint,
                            focusedTrailingIconColor = iconTint,
                            unfocusedTrailingIconColor = iconTint,
                            focusedPlaceholderColor = placeholderColor,
                            unfocusedPlaceholderColor = placeholderColor
                        )
                        BasicTextField(
                            value = text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(32.dp, 44.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                textDirection = TextDirection.Content,
                                color = textColor,
                                fontSize = 16.sp
                            ),
                            onValueChange = {
                                text = it
                                search(it.text)
                            },
                            enabled = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                platformImeOptions = PlatformImeOptions(
                                    encodePrivateImeOptions()
                                )
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                if (Settings.getInstance().current.mAutoCorrectionEnabledPerUserSettings) {
                                    pressedKey = firstKey
                                }
                                finish()
                            }),
                            singleLine = true,
                            cursorBrush = SolidColor(textColor)
                        ) { innerTextField ->
                            TextFieldDefaults.DecorationBox(
                                value = text.text,
                                colors = textFieldColors,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                visualTransformation = VisualTransformation.None,
                                innerTextField = innerTextField,
                                placeholder = {
                                    Text(
                                        stringResource(R.string.search_field_placeholder),
                                        color = placeholderColor
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.sym_keyboard_search_lxx_light),
                                        contentDescription = stringResource(R.string.spoken_description_emoji_search),
                                        tint = iconTint
                                    )
                                },
                                trailingIcon = {
                                    if (text.text.isNotEmpty()) {
                                        IconButton(onClick = {
                                            text = TextFieldValue()
                                            search("")
                                        }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_close),
                                                contentDescription = stringResource(R.string.not_now),
                                                tint = iconTint
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                enabled = true,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                        }
                        val windowInfo = LocalWindowInfo.current
                        LaunchedEffect(windowInfo.isWindowFocused) {
                            if (windowInfo.isWindowFocused) {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_TEXT", currentSearchText)
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        search(currentSearchText)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        init()
        firstSearchDone = false
        search(currentSearchText)
    }

    override fun onStop() {
        pressedKey?.let {
            val emojiText: String = (it.outputText ?: if (it.code > 0) Character.toString(it.code.toChar()) else "")
            if (emojiText.isNotEmpty()) {
                LatinIME.onEmojiSearchCompleted(emojiText)
            }
            KeyboardSwitcher.getInstance().emojiPalettesView?.addRecentKey(it)
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDictionaryFacilitator()
    }

    private fun init() {
        @Suppress("DEPRECATION")
        screenHeight = windowManager.defaultDisplay.height
        hintLocales = LocaleList(
            DictionaryInfoUtils.getLocalesWithEmojiDicts(this).map { Locale(it.toLanguageTag()) }
        )
        val keyboardWidth = ResourceUtils.getDefaultKeyboardWidth(resources)
        val emojiHeight = EmojiLayoutParams(resources).mEmojiKeyboardHeight
        val layoutSet = KeyboardLayoutSet.Builder(this, null)
            .setSubtype(RichInputMethodSubtype.getEmojiSubtype())
            .setKeyboardGeometry(keyboardWidth, emojiHeight)
            .build()
        templateKeyboard = layoutSet.getKeyboard(KeyboardId.ELEMENT_EMOJI_RECENTS)
        val keyboard = DynamicGridKeyboard.ofRowCount(
            PreferenceManager.getDefaultSharedPreferences(this),
            templateKeyboard,
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 1 else 2,
            false,
            keyboardWidth
        )
        for (k in templateKeyboard.sortedKeys) {
            if (k.code == 0x30) {
                templateKey0 = k
                break
            }
        }
        emojiPageKeyboardView = EmojiPageKeyboardView(this, null)
        emojiPageKeyboardView.setKeyboard(keyboard)
        emojiPageKeyboardView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        emojiPageKeyboardView.setPadding(0, 10, 0, 10)

        emojiPageKeyboardView.setOnKeyEventListener(object : OnKeyEventListener {
            override fun onPressKey(key: Key) {}
            override fun onReleaseKey(key: Key) {
                pressedKey = key
                finish()
            }
        })
        KeyboardSwitcher.getInstance().setAlphabetKeyboard()
    }

    private fun isAlphaKeyboard(): Boolean {
        val switcher = KeyboardSwitcher.getInstance()
        return !switcher.isShowingEmojiPalettes && !switcher.isShowingClipboardHistory
    }

    private fun search(text: String) {
        initDictionaryFacilitator(this, templateKeyboard.proximityInfo)
        val facilitator = dictionaryFacilitator ?: return

        if (firstSearchDone && text == currentSearchText) {
            return
        }

        val keyboard = emojiPageKeyboardView.keyboard as? DynamicGridKeyboard ?: return
        keyboard.removeAllKeys()
        firstKey = null
        pressedKey = null

        val key0 = templateKey0 ?: return
        val keyWidth = key0.width + key0.horizontalGap
        val keyHeight = key0.height + key0.verticalGap

        val tokens = text.splitOnWhitespace()
        val results = facilitator.getSuggestions(tokens)
        for (info in results) {
            if (info.isEmoji) {
                val emoji = info.word
                val key = Key(
                    emoji,
                    KeyboardIconsSet.ICON_UNDEFINED,
                    Constants.CODE_OUTPUT_TEXT,
                    emoji,
                    null,
                    Key.LABEL_FLAGS_FONT_NORMAL,
                    Key.BACKGROUND_TYPE_EMPTY,
                    key0.x, key0.y,
                    keyWidth, keyHeight,
                    key0.horizontalGap, key0.verticalGap
                )
                keyboard.addKeyLast(key)
                if (firstKey == null) firstKey = key
            }
        }
        emojiPageKeyboardView.invalidateAllKeys()
        emojiPageKeyboardView.requestLayout()

        currentSearchText = text
        firstSearchDone = true
    }

    private fun cancel() {
        finish()
    }

    companion object {
        const val PRIVATE_IME_OPTIONS_PREFIX: String = "org.dslul.openboard.inputmethod.keyboard.emoji.search"
        private var dictionaryFacilitator: SingleDictionaryFacilitator? = null

        fun isEmojiSearch(editorInfo: EditorInfo?): Boolean {
            val privateOptions = editorInfo?.privateImeOptions ?: return false
            return privateOptions.startsWith(PRIVATE_IME_OPTIONS_PREFIX)
        }

        fun closeDictionaryFacilitator() {
            dictionaryFacilitator?.closeDictionaries()
            dictionaryFacilitator = null
        }

        private fun encodePrivateImeOptions() = "$PRIVATE_IME_OPTIONS_PREFIX,"

        private fun initDictionaryFacilitator(context: Context, proximityInfo: ProximityInfo? = null) {
            RichInputMethodManager.init(context)
            val locale = RichInputMethodManager.getInstance().currentSubtypeLocale
            if (dictionaryFacilitator?.isForLocale(locale) != true) {
                dictionaryFacilitator?.closeDictionaries()
                val dictFile = DictionaryInfoUtils.getCachedDictForLocaleAndType(locale, Dictionary.TYPE_EMOJI, context)
                val dict = if (dictFile != null) DictionaryFactory.getDictionary(dictFile, locale) else null
                dictionaryFacilitator = if (dict != null) SingleDictionaryFacilitator(dict, proximityInfo) else null
            }
        }
    }
}
