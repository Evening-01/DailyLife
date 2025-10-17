package com.evening.dailylife.core.util

import kotlin.random.Random

/**
 * 随机字符串生成工具，可按需拼接字符集。
 */
object RandomStringGenerator {

    fun generate(
        length: Int,
        includeLowerCase: Boolean = false,
        includeUpperCase: Boolean = false,
        includeDigits: Boolean = false,
        includePunctuation: Boolean = false
    ): String {
        val lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"

        var allowedChars = ""
        if (includeLowerCase) allowedChars += lowerCaseLetters
        if (includeUpperCase) allowedChars += upperCaseLetters
        if (includeDigits) allowedChars += digits
        if (includePunctuation) allowedChars += punctuation

        return (1..length)
            .map { allowedChars.random(Random) }
            .joinToString("")
    }
}
