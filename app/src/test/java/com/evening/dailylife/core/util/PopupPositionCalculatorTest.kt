package com.evening.dailylife.core.util

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test


class PopupPositionCalculatorTest {

    @Test
    fun `calculateOffset clamps to minimal margin`() {
        val offset = PopupPositionCalculator.calculateOffset(
            density = 3f,
            clickOffsetX = 10f,
            popupWidth = 120,
            screenWidthDp = 360
        )
        assertEquals(16f, offset)
    }

    @Test
    fun `calculateOffset clamps to screen width`() {
        val offset = PopupPositionCalculator.calculateOffset(
            density = 3f,
            clickOffsetX = 1000f,
            popupWidth = 200,
            screenWidthDp = 360
        )
        assertTrue(offset <= 360 - 200 - 42)
    }

    @Test
    fun `calculateOffset centers around anchor when space allows`() {
        val offset = PopupPositionCalculator.calculateOffset(
            density = 3f,
            clickOffsetX = 500f,
            popupWidth = 160,
            screenWidthDp = 360
        )
        assertTrue(offset > 16f && offset < 360 - 160 - 42)
    }
}
