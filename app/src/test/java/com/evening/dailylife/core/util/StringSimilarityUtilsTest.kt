package com.evening.dailylife.core.util

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class StringSimilarityUtilsTest {

    @Test
    fun `similarDegree returns 1 for identical strings`() {
        assertEquals(1.0, StringSimilarityUtils.similarDegree("记账助手", "记账助手"))
    }

    @Test
    fun `similarDegree ignores punctuation`() {
        val value = StringSimilarityUtils.similarDegree("记账-助手", "记账助手")
        assertTrue(value > 0.9)
    }

    @Test
    fun `similarityRatio reflects difference`() {
        val value = StringSimilarityUtils.similarityRatio("abc", "axc")
        assertTrue(value < 1.0)
        assertTrue(value > 0.5)
    }
}
