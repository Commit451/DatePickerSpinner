package com.commit451.datepickerspinner

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Formats the values shown by a [DatePickerSpinner] and controls the left-to-right order of its
 * wheels.
 *
 * Obtain the default implementation from [DatePickerSpinnerDefaults.dateFormatter], or implement
 * this interface directly for full control — for example to localize month names, render days
 * with a locale's numerals, or append era information to the year.
 */
@Stable
interface DatePickerSpinnerFormatter {

    /** The left-to-right order of the month, day and year wheels. */
    val fieldOrder: List<DateField>

    /** Formats the given 1-based [month], where 1 is January and 12 is December. */
    fun formatMonth(month: Int): String

    /** Formats the given 1-based [day] of the month. */
    fun formatDay(day: Int): String

    /** Formats the given [year]. */
    fun formatYear(year: Int): String
}

/**
 * The default [DatePickerSpinnerFormatter]: a fixed list of [monthNames], a fixed [fieldOrder],
 * and plain numeric day and year labels. Created by [DatePickerSpinnerDefaults.dateFormatter].
 */
@Immutable
internal class DefaultDatePickerSpinnerFormatter(
    private val monthNames: List<String>,
    override val fieldOrder: List<DateField>,
) : DatePickerSpinnerFormatter {

    override fun formatMonth(month: Int): String = monthNames[month - 1]

    override fun formatDay(day: Int): String = day.toString()

    override fun formatYear(year: Int): String = year.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultDatePickerSpinnerFormatter) return false
        return monthNames == other.monthNames && fieldOrder == other.fieldOrder
    }

    override fun hashCode(): Int = 31 * monthNames.hashCode() + fieldOrder.hashCode()

    override fun toString(): String =
        "DefaultDatePickerSpinnerFormatter(monthNames=$monthNames, fieldOrder=$fieldOrder)"
}
