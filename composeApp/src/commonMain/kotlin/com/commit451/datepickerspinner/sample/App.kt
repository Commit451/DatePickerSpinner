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
    val systemInDarkTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemInDarkTheme) }

    // Keep the system bar icon contrast in sync with the chosen theme.
    SystemAppearance(isDark = darkTheme)

    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Inset content within the status/navigation bars so nothing draws under them.
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
    }
}
