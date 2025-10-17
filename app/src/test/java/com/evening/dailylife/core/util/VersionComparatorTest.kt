package com.evening.dailylife.core.util

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `isNewer returns true when candidate has higher major`() {
        assertTrue(VersionComparator.isNewer("1.2.0", "2.0.0"))
    }

    @Test
    fun `isNewer returns true when candidate has higher minor`() {
        assertTrue(VersionComparator.isNewer("1.2.0", "1.3.0"))
    }

    @Test
    fun `isNewer returns false when candidate is older`() {
        assertFalse(VersionComparator.isNewer("1.2.0", "1.1.9"))
    }
}
