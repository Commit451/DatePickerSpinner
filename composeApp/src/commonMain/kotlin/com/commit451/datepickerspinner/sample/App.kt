package com.commit451.datepickerspinner.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.commit451.datepickerspinner.DatePickerSpinner

@Composable
fun App() {
    MaterialTheme {
        DatePickerSpinner(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(),
        )
    }
}
