package com.commit451.datepickerspinner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateMathTest {

    @Test
    fun monthsWith31Days() {
        listOf(1, 3, 5, 7, 8, 10, 12).forEach { month ->
            assertEquals(31, daysInMonth(2026, month), "month $month")
        }
    }

    @Test
    fun monthsWith30Days() {
        listOf(4, 6, 9, 11).forEach { month ->
            assertEquals(30, daysInMonth(2026, month), "month $month")
        }
    }

    @Test
    fun februaryHas28DaysInACommonYear() {
        assertEquals(28, daysInMonth(2026, 2))
    }

    @Test
    fun februaryHas29DaysInALeapYear() {
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(29, daysInMonth(2000, 2))
    }

    @Test
    fun leapYearRules() {
        assertTrue(isLeapYear(2024), "divisible by 4")
        assertTrue(isLeapYear(2000), "divisible by 400")
        assertFalse(isLeapYear(2023), "not divisible by 4")
        assertFalse(isLeapYear(1900), "divisible by 100 but not 400")
        assertFalse(isLeapYear(2100), "divisible by 100 but not 400")
    }
}
