package com.commit451.datepickerspinner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A spinner-style date picker.
 *
 * This is currently a stub. Replace the body with the real spinner implementation.
 */
@Composable
fun DatePickerSpinner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text = "DatePickerSpinner")
    }
}
