package com.evening.dailylife.core.util

/**
 * 字符串相似度相关工具。
 */
object StringSimilarityUtils {

    fun similarDegree(strA: String, strB: String): Double {
        val newStrA = removeSign(strA)
        val newStrB = removeSign(strB)
        val maxLength = newStrA.length.coerceAtLeast(newStrB.length)
        val commonLength = longestCommonSubstringNoOrder(newStrA, newStrB).length
        return commonLength * 1.0 / maxLength
    }

    fun similarityRatio(strA: String, strB: String): Double {
        return 1 - compare(strA, strB).toDouble() / strA.length.coerceAtLeast(strB.length)
    }

    private fun longestCommonSubstringNoOrder(strA: String, strB: String): String {
        return if (strA.length >= strB.length) {
            longestCommonSubstring(strA, strB)
        } else {
            longestCommonSubstring(strB, strA)
        }
    }

    private fun longestCommonSubstring(strLong: String, strShort: String): String {
        val charsStrA = strLong.toCharArray()
        val charsStrB = strShort.toCharArray()
        var m = charsStrA.size
        var n = charsStrB.size
        val matrix = Array(m + 1) {
            IntArray(n + 1)
        }
        for (i in 1..m) {
            for (j in 1..n) {
                if (charsStrA[i - 1] == charsStrB[j - 1]) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1
                } else {
                    matrix[i][j] = matrix[i][j - 1].coerceAtLeast(matrix[i - 1][j])
                }
            }
        }
        val result = CharArray(matrix[m][n])
        var currentIndex = result.size - 1
        while (matrix[m][n] != 0) {
            if (matrix[n].contentEquals(matrix[n - 1])) {
                n--
            } else if (matrix[m][n] == matrix[m - 1][n]) {
                m--
            } else {
                result[currentIndex] = charsStrA[m - 1]
                currentIndex--
                n--
                m--
            }
        }
        return String(result)
    }

    private fun removeSign(str: String): String {
        val sb = StringBuilder()
        for (item in str.toCharArray()) {
            if (charReg(item)) {
                sb.append(item)
            }
        }
        return sb.toString()
    }

    private fun charReg(charValue: Char): Boolean {
        return charValue.code in 0x4E00..0X9FA5 || charValue in 'a'..'z' || charValue in 'A'..'Z' || charValue in '0'..'9'
    }

    private fun compare(str: String, target: String): Int {
        val n = str.length
        val m = target.length
        if (n == 0) return m
        if (m == 0) return n

        val matrix = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) matrix[i][0] = i
        for (j in 0..m) matrix[0][j] = j

        for (i in 1..n) {
            val ch1 = str[i - 1]
            for (j in 1..m) {
                val ch2 = target[j - 1]
                val temp = if (ch1 == ch2) 0 else 1
                matrix[i][j] = min(
                    matrix[i - 1][j] + 1,
                    matrix[i][j - 1] + 1,
                    matrix[i - 1][j - 1] + temp
                )
            }
        }
        return matrix[n][m]
    }

    private fun min(one: Int, two: Int, three: Int): Int {
        val minOfTwo = one.coerceAtMost(two)
        return if (minOfTwo < three) minOfTwo else three
    }
}
