package com.commit451.datepickerspinner.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.commit451.datepickerspinner.DatePickerSpinner

/**
 * The sample app.
 *
 * @param nativeDatePicker optional slot for a platform-native date picker, shown below our
 * [DatePickerSpinner] so the two can be compared side by side. The Android sample passes the
 * View-based [android.widget.DatePicker] in its spinner mode here.
 */
@Composable
fun App(
    nativeDatePicker: @Composable () -> Unit = {},
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "DatePickerSpinner (ours)",
                style = MaterialTheme.typography.titleMedium,
            )
            DatePickerSpinner()

            Text(
                text = "Android DatePicker (spinner)",
                style = MaterialTheme.typography.titleMedium,
            )
            nativeDatePicker()
        }
    }
}
