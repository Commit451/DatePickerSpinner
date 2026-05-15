package com.commit451.datepickerspinner

/**
 * A simple, time-zone-free calendar date.
 *
 * @param year the full year, e.g. 2026
 * @param month the month of the year, 1 (January) through 12 (December)
 * @param dayOfMonth the day of the month, 1-based
 */
data class CalendarDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
)

/** The number of days in the given 1-based [month] of [year], accounting for leap years. */
internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

internal fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
