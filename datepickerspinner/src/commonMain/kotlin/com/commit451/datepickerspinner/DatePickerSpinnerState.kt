package com.commit451.datepickerspinner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Creates and remembers a [DatePickerSpinnerState]. The state is saved and restored across
 * configuration changes and process death.
 *
 * @param initialDate the date selected when the picker first appears. Defaults to today.
 * @param yearRange the inclusive range of selectable years.
 */
@Composable
fun rememberDatePickerSpinnerState(
    initialDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    yearRange: IntRange = DatePickerSpinnerDefaults.YearRange,
): DatePickerSpinnerState =
    rememberSaveable(saver = DatePickerSpinnerState.Saver) {
        DatePickerSpinnerState(initialDate, yearRange)
    }

/**
 * The hoisted state of a [DatePickerSpinner].
 *
 * Holds the current selection and keeps it valid — for example clamping the day when a change of
 * month or year shortens the month. Read or assign [selectedDate] to observe or drive the picker.
 */
@Stable
class DatePickerSpinnerState(
    initialDate: LocalDate,
    val yearRange: IntRange = DatePickerSpinnerDefaults.YearRange,
) {
    private var year by mutableIntStateOf(initialDate.year.coerceIn(yearRange))
    private var month by mutableIntStateOf(initialDate.month.ordinal + 1)
    private var day by mutableIntStateOf(
        initialDate.day.coerceAtMost(daysInMonth(year, month)),
    )

    /** The currently selected date. Assigning a value moves the picker to that date. */
    var selectedDate: LocalDate
        get() = LocalDate(year, month, day)
        set(value) {
            year = value.year.coerceIn(yearRange)
            month = value.month.ordinal + 1
            day = value.day.coerceAtMost(daysInMonth(year, month))
        }

    internal val selectedYear: Int get() = year
    internal val selectedMonth: Int get() = month
    internal val selectedDay: Int get() = day

    internal fun selectYear(value: Int) {
        year = value.coerceIn(yearRange)
        day = day.coerceAtMost(daysInMonth(year, month))
    }

    internal fun selectMonth(value: Int) {
        month = value.coerceIn(1, 12)
        day = day.coerceAtMost(daysInMonth(year, month))
    }

    internal fun selectDay(value: Int) {
        day = value.coerceIn(1, daysInMonth(year, month))
    }

    companion object {
        /** A [Saver] for [DatePickerSpinnerState], used by [rememberDatePickerSpinnerState]. */
        val Saver: Saver<DatePickerSpinnerState, *> = listSaver(
            save = { listOf(it.year, it.month, it.day, it.yearRange.first, it.yearRange.last) },
            restore = {
                DatePickerSpinnerState(
                    initialDate = LocalDate(it[0], it[1], it[2]),
                    yearRange = it[3]..it[4],
                )
            },
        )
    }
}
