package com.commit451.datepickerspinner.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.commit451.datepickerspinner.DatePickerSpinner

@Composable
fun App(
    nativeDatePicker: @Composable () -> Unit = {},
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemInDarkTheme) }

    SystemAppearance(isDark = darkTheme)

    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = if (darkTheme) "Dark" else "Light")
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { darkTheme = it },
                    )
                }

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "DatePickerSpinner (fillMaxWidth)",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    DatePickerSpinner(
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "DatePickerSpinner (compact)",
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
    }
}
