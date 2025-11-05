package com.evening.dailylife.core.data.analytics

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDateUtilsTest {

    @Test
    fun dayStartMillis_alignsToLocalMidnight_whenTimestampIsMidday() {
        val zone = ZoneId.of("Asia/Shanghai")
        val afternoon = LocalDateTime.of(2024, 11, 5, 15, 30, 45)
        val timestamp = afternoon.atZone(zone).toInstant().toEpochMilli()

        val expected = LocalDate.of(2024, 11, 5)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val actual = TransactionDateUtils.dayStartMillis(timestamp, zone)

        assertEquals(expected, actual)
    }

    @Test
    fun dayStartMillis_preservesLocalMidnight_whenTimestampIsExactlyMidnight() {
        val zone = ZoneId.of("Asia/Shanghai")
        val midnight = LocalDate.of(2024, 11, 5)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val actual = TransactionDateUtils.dayStartMillis(midnight, zone)

        assertEquals(midnight, actual)
    }
}
