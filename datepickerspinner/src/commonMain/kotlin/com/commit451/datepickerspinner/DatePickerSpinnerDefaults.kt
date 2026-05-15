package com.commit451.datepickerspinner

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Colors used to render a [DatePickerSpinner].
 *
 * Create instances with [DatePickerSpinnerDefaults.colors].
 */
@Immutable
class DatePickerSpinnerColors(
    /** Color of the centered, currently-selected value. */
    val selectedTextColor: Color,
    /** Color of the dimmed values above and below the selection. */
    val unselectedTextColor: Color,
    /** Color of the two lines framing the selected value. */
    val dividerColor: Color,
) {
    /** Returns a copy of this [DatePickerSpinnerColors], optionally overriding some values. */
    fun copy(
        selectedTextColor: Color = this.selectedTextColor,
        unselectedTextColor: Color = this.unselectedTextColor,
        dividerColor: Color = this.dividerColor,
    ): DatePickerSpinnerColors = DatePickerSpinnerColors(
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        dividerColor = dividerColor,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatePickerSpinnerColors) return false
        return selectedTextColor == other.selectedTextColor &&
            unselectedTextColor == other.unselectedTextColor &&
            dividerColor == other.dividerColor
    }

    override fun hashCode(): Int {
        var result = selectedTextColor.hashCode()
        result = 31 * result + unselectedTextColor.hashCode()
        result = 31 * result + dividerColor.hashCode()
        return result
    }

    override fun toString(): String =
        "DatePickerSpinnerColors(" +
            "selectedTextColor=$selectedTextColor, " +
            "unselectedTextColor=$unselectedTextColor, " +
            "dividerColor=$dividerColor)"
}

/** Default values used by [DatePickerSpinner], all derived from the ambient [MaterialTheme]. */
object DatePickerSpinnerDefaults {

    /** The default text style applied to every wheel value. */
    val textStyle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge

    /**
     * Creates the default [DatePickerSpinnerColors], resolved from the current [MaterialTheme].
     *
     * Override individual arguments to tweak a single picker without changing the app-wide theme.
     */
    @Composable
    fun colors(
        selectedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        dividerColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): DatePickerSpinnerColors = DatePickerSpinnerColors(
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        dividerColor = dividerColor,
    )
}
