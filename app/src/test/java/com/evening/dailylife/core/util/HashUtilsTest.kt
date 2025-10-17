package com.evening.dailylife.core.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `md5 returns expected 32 length hash`() {
        val result = HashUtils.md5("daily life")
        assertEquals(32, result.length)
        assertEquals("04639e912ec8537dc2b961669e1760a6", result)
    }

    @Test
    fun `md5 supports uppercase 16 length mode`() {
        val result = HashUtils.md5("daily life", length = 16, toUpperCase = true)
        assertEquals(16, result.length)
        assertEquals("2EC8537DC2B96166", result)
    }
}
