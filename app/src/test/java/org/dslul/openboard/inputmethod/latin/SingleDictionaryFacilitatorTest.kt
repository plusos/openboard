// SPDX-License-Identifier: Apache-2.0
package org.dslul.openboard.inputmethod.latin

import org.dslul.openboard.inputmethod.keyboard.ProximityInfo
import org.dslul.openboard.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.dslul.openboard.inputmethod.latin.common.ComposedData
import org.dslul.openboard.inputmethod.latin.common.Constants
import org.dslul.openboard.inputmethod.latin.settings.SettingsValuesForSuggestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayList
import java.util.Locale

class SingleDictionaryFacilitatorTest {

    private class FakeDictionary(val suggestionsMap: Map<String, List<SuggestedWordInfo>>) :
        Dictionary(TYPE_EMOJI, Locale.ENGLISH) {

        var lastComposedData: ComposedData? = null
        var lastProximityInfoHandle: Long = -1L

        override fun getSuggestions(
            composedData: ComposedData,
            ngramContext: NgramContext,
            proximityInfoHandle: Long,
            settingsValuesForSuggestion: SettingsValuesForSuggestion,
            sessionId: Int,
            weightForLocale: Float,
            inOutWeightOfLangModelVsSpatialModel: FloatArray?
        ): ArrayList<SuggestedWordInfo>? {
            lastComposedData = composedData
            lastProximityInfoHandle = proximityInfoHandle
            val word = composedData.mTypedWord
            val results = suggestionsMap[word] ?: return ArrayList()
            return ArrayList(results)
        }

        override fun isInDictionary(word: String?): Boolean = false
        override fun getFrequency(word: String?): Int = NOT_A_PROBABILITY
        override fun getMaxFrequencyOfExactMatches(word: String?): Int = NOT_A_PROBABILITY
    }

    @Test
    fun testEmptyWordsReturnsEmptyResults() {
        val fakeDict = FakeDictionary(emptyMap())
        val facilitator = SingleDictionaryFacilitator(fakeDict)
        val results = facilitator.getSuggestions(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    fun testSingleCharWordCreatesValidInputPointers() {
        val fakeDict = FakeDictionary(emptyMap())
        val facilitator = SingleDictionaryFacilitator(fakeDict)
        facilitator.getSuggestions(listOf("a"))

        val composedData = fakeDict.lastComposedData
        assertNotNull(composedData)
        assertEquals("a", composedData?.mTypedWord)
        assertNotNull(composedData?.mInputPointers)
        val xCoords = composedData?.mInputPointers?.xCoordinates
        assertNotNull(xCoords)
        assertTrue(xCoords!!.size >= 1)
        assertEquals(Constants.NOT_A_COORDINATE, xCoords[0])
    }

    @Test
    fun testMultiCharWordCreatesValidInputPointersWithoutOutOfBounds() {
        val word = "smile"
        val fakeDict = FakeDictionary(
            mapOf(
                word to listOf(
                    SuggestedWordInfo("😀", "", 100, SuggestedWordInfo.KIND_TYPED, null, -1, -1)
                )
            )
        )
        val facilitator = SingleDictionaryFacilitator(fakeDict)
        val results = facilitator.getSuggestions(listOf(word))

        assertEquals(1, results.size)
        assertEquals("😀", results.first().mWord)

        val composedData = fakeDict.lastComposedData
        assertNotNull(composedData)
        assertEquals(word, composedData?.mTypedWord)
        val pointers = composedData?.mInputPointers
        assertNotNull(pointers)
        val xCoords = pointers!!.xCoordinates
        val yCoords = pointers.yCoordinates
        val times = pointers.times
        val pointerIds = pointers.pointerIds

        // Crucial check: all coordinate arrays must be at least word.length in length
        assertTrue(xCoords.size >= word.length)
        assertTrue(yCoords.size >= word.length)
        assertTrue(times.size >= word.length)
        assertTrue(pointerIds.size >= word.length)

        for (i in 0 until word.length) {
            assertEquals(Constants.NOT_A_COORDINATE, xCoords[i])
            assertEquals(Constants.NOT_A_COORDINATE, yCoords[i])
        }
    }

    @Test
    fun testMultiTokenSearchMergesResults() {
        val fakeDict = FakeDictionary(
            mapOf(
                "cat" to listOf(
                    SuggestedWordInfo("🐱", "", 50, SuggestedWordInfo.KIND_TYPED, null, -1, -1),
                    SuggestedWordInfo("😹", "", 60, SuggestedWordInfo.KIND_TYPED, null, -1, -1),
                    SuggestedWordInfo("🐶", "", 40, SuggestedWordInfo.KIND_TYPED, null, -1, -1)
                ),
                "joy" to listOf(
                    SuggestedWordInfo("😹", "", 50, SuggestedWordInfo.KIND_TYPED, null, -1, -1),
                    SuggestedWordInfo("😂", "", 80, SuggestedWordInfo.KIND_TYPED, null, -1, -1)
                )
            )
        )
        val facilitator = SingleDictionaryFacilitator(fakeDict)
        val results = facilitator.getSuggestions(listOf("cat", "joy"))

        // Only "😹" is present in both tokens
        assertEquals(1, results.size)
        assertEquals("😹", results.first().mWord)
        // Score should be sum of 60 + 50 = 110
        assertEquals(110, results.first().mScore)
    }

    @Test
    fun testProximityInfoHandlePropagation() {
        val fakeDict = FakeDictionary(emptyMap())
        val dummy = ProximityInfo.createDummyProximityInfo()
        val facilitator = SingleDictionaryFacilitator(fakeDict, dummy)
        facilitator.getSuggestions(listOf("smile"))

        assertEquals(dummy?.nativeProximityInfo ?: 0L, fakeDict.lastProximityInfoHandle)
    }
}
