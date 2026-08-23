// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package org.dslul.openboard.inputmethod.latin

import android.content.Context
import android.util.LruCache
import org.dslul.openboard.inputmethod.keyboard.Keyboard
import org.dslul.openboard.inputmethod.keyboard.ProximityInfo
import org.dslul.openboard.inputmethod.latin.common.ComposedData
import org.dslul.openboard.inputmethod.latin.common.InputPointers
import org.dslul.openboard.inputmethod.latin.settings.SettingsValuesForSuggestion
import org.dslul.openboard.inputmethod.latin.utils.SuggestionResults
import org.dslul.openboard.inputmethod.latin.common.Constants
import org.dslul.openboard.inputmethod.latin.define.DecoderSpecificConstants
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.concurrent.TimeUnit

class SingleDictionaryFacilitator(
    private val dict: Dictionary,
    private var proximityInfo: ProximityInfo? = null
) : DictionaryFacilitator {
    private val dummyProximityInfo by lazy {
        try {
            ProximityInfo.createDummyProximityInfo()
        } catch (t: Throwable) {
            null
        }
    }

    private fun getProximityInfoHandle(keyboard: Keyboard? = null): Long {
        if (keyboard?.proximityInfo != null) {
            val handle = keyboard.proximityInfo.nativeProximityInfo
            if (handle != 0L) return handle
        }
        if (proximityInfo != null && proximityInfo!!.nativeProximityInfo != 0L) {
            return proximityInfo!!.nativeProximityInfo
        }
        return dummyProximityInfo?.nativeProximityInfo ?: 0L
    }

    private fun createInputPointers(word: String): InputPointers {
        val length = word.codePointCount(0, word.length)
        val capacity = Math.max(DecoderSpecificConstants.DICTIONARY_MAX_WORD_LENGTH, length)
        val inputPointers = InputPointers(capacity)
        for (i in 0 until length) {
            inputPointers.addPointerAt(i, Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE, 0, 0)
        }
        return inputPointers
    }

    fun getSuggestions(words: List<String>): SuggestionResults {
        val word = words.firstOrNull() ?: return SuggestionResults(100, false, false)
        val composedData = ComposedData(createInputPointers(word), false, word)
        val ngramContext = NgramContext.BEGINNING_OF_SENTENCE
        val settings = SettingsValuesForSuggestion(false)
        val proximityInfoHandle = getProximityInfoHandle()
        val initialResults = dict.getSuggestions(
            composedData,
            ngramContext,
            proximityInfoHandle,
            settings,
            1,
            1.0f,
            null
        ) ?: ArrayList()

        val mergedResults = SuggestionResults(100, false, false)
        for (info in initialResults) {
            mergedResults.add(info)
        }

        for (i in 1 until words.size) {
            val nextWord = words[i]
            val nextComposedData = ComposedData(createInputPointers(nextWord), false, nextWord)
            val nextResults = dict.getSuggestions(
                nextComposedData,
                ngramContext,
                proximityInfoHandle,
                settings,
                1,
                1.0f,
                null
            ) ?: continue

            val scoreMap = HashMap<String, Int>()
            for (info in nextResults) {
                scoreMap[info.mWord] = info.mScore
            }

            val iterator = mergedResults.iterator()
            val toKeep = ArrayList<SuggestedWords.SuggestedWordInfo>()
            while (iterator.hasNext()) {
                val current = iterator.next()
                val score = scoreMap[current.mWord]
                if (score != null) {
                    val updated = SuggestedWords.SuggestedWordInfo(
                        current.mWord,
                        current.mPrevWordsContext,
                        current.mScore + score,
                        current.mKindAndFlags,
                        current.mSourceDict,
                        current.mIndexOfTouchPointOfSecondWord,
                        current.mAutoCommitFirstWordConfidence
                    )
                    toKeep.add(updated)
                }
            }
            mergedResults.clear()
            for (item in toKeep) {
                mergedResults.add(item)
            }
        }
        return mergedResults
    }

    override fun isForLocale(locale: Locale?): Boolean {
        if (locale == null || dict.mLocale == null) return false
        return dict.mLocale.language == locale.language
    }

    override fun isForAccount(account: String?): Boolean = true

    override fun setValidSpellingWordReadCache(cache: LruCache<String, Boolean>?) {}
    override fun setValidSpellingWordWriteCache(cache: LruCache<String, Boolean>?) {}

    override fun onStartInput() {}
    override fun onFinishInput(context: Context?) {}
    override fun isActive(): Boolean = true
    override fun getLocale(): Locale? = dict.mLocale
    override fun usesContacts(): Boolean = false
    override fun getAccount(): String? = null

    override fun resetDictionaries(
        context: Context?,
        newLocale: Locale?,
        useContactsDict: Boolean,
        usePersonalizedDicts: Boolean,
        forceReloadMainDictionary: Boolean,
        account: String?,
        dictNamePrefix: String?,
        listener: DictionaryFacilitator.DictionaryInitializationListener?
    ) {}

    override fun resetDictionariesForTesting(
        context: Context?,
        locale: Locale?,
        dictionaryTypes: ArrayList<String>?,
        dictionaryFiles: HashMap<String, File>?,
        additionalDictAttributes: MutableMap<String, MutableMap<String, String>>?,
        account: String?
    ) {}

    override fun closeDictionaries() {
        dict.close()
        proximityInfo = null
    }

    override fun getSubDictForTesting(dictName: String?): ExpandableBinaryDictionary? = null
    override fun hasAtLeastOneInitializedMainDictionary(): Boolean = true
    override fun hasAtLeastOneUninitializedMainDictionary(): Boolean = false
    override fun waitForLoadingMainDictionaries(timeout: Long, unit: TimeUnit?) {}
    override fun waitForLoadingDictionariesForTesting(timeout: Long, unit: TimeUnit?) {}

    override fun addToUserHistory(
        suggestion: String?,
        wasAutoCapitalized: Boolean,
        ngramContext: NgramContext,
        timeStampInSeconds: Long,
        blockPotentiallyOffensive: Boolean
    ) {}

    override fun unlearnFromUserHistory(
        word: String?,
        ngramContext: NgramContext,
        timeStampInSeconds: Long,
        eventType: Int
    ) {}

    override fun getSuggestionResults(
        composedData: ComposedData?,
        ngramContext: NgramContext?,
        keyboard: Keyboard,
        settingsValuesForSuggestion: SettingsValuesForSuggestion?,
        sessionId: Int,
        inputStyle: Int
    ): SuggestionResults {
        val suggestionResults = SuggestionResults(100, false, false)
        if (composedData == null) return suggestionResults
        val settings = settingsValuesForSuggestion ?: SettingsValuesForSuggestion(false)
        val proximityInfoHandle = getProximityInfoHandle(keyboard)
        val results = dict.getSuggestions(
            composedData,
            ngramContext ?: NgramContext.BEGINNING_OF_SENTENCE,
            proximityInfoHandle,
            settings,
            sessionId,
            1.0f,
            null
        )
        if (results != null) {
            for (info in results) {
                suggestionResults.add(info)
            }
        }
        return suggestionResults
    }

    override fun isValidSpellingWord(word: String?): Boolean = false
    override fun isValidSuggestionWord(word: String?): Boolean = dict.isValidWord(word)
    override fun clearUserHistoryDictionary(context: Context?): Boolean = false
    override fun dump(context: Context?): String = "SingleDictionaryFacilitator"
    override fun dumpDictionaryForDebug(dictName: String?) {}
    override fun getDictionaryStats(context: Context?): List<DictionaryStats> = emptyList()
}
