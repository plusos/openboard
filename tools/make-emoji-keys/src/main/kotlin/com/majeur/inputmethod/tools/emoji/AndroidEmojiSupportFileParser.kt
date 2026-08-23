package com.majeur.inputmethod.tools.emoji


class AndroidEmojiSupportFileParser : TextFileParser<Map<String, Int>>() {

    private val map = mutableMapOf<String, Int>()
    private var currentApiLevel = 0

    override fun getParseResult() = map

    override fun parseLine(content: String) {
        ifStartsWith(content,
            API_LEVEL_MARK to ::parseApiLevel,
            UNICODE_MARK to ::parseCodePoints)
    }

    private fun parseApiLevel(content: String) {
        currentApiLevel = content
                .substringBefore("#")
                .trim()
                .toInt()
    }

    private fun parseCodePoints(content: String) {
        val codePointsKey = content
                .substringBefore("#")
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .map { it
                        .trim()
                        .removePrefix("U+")
                        .toInt(radix = 16) }
                .joinToString(separator = ",")
        map[codePointsKey] = currentApiLevel
    }

    companion object {

        private const val API_LEVEL_MARK = "@"
        private const val UNICODE_MARK = "U"
    }
}