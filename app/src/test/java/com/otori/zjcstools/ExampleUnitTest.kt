package com.otori.zjcstools

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ExampleUnitTest {
    @Test
    fun resetDate_beforeEightBelongsToPreviousDay() {
        val date = LocalDate.of(2026, 6, 10)

        assertEquals(
            LocalDate.of(2026, 6, 9),
            resetDateFor(date, LocalTime.of(7, 59))
        )
    }

    @Test
    fun resetDate_atEightBelongsToCurrentDay() {
        val date = LocalDate.of(2026, 6, 10)

        assertEquals(
            date,
            resetDateFor(date, LocalTime.of(8, 0))
        )
    }
}
