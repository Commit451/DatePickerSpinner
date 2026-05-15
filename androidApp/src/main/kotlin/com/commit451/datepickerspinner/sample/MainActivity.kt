package com.commit451.datepickerspinner.sample

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.key
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(
                nativeDatePicker = { isDark ->
                    // The View-based DatePicker is themed via its Context, not Compose's
                    // MaterialTheme. Rebuild it under a matching platform theme (key(isDark))
                    // whenever the light/dark toggle changes.
                    key(isDark) {
                        AndroidView(
                            factory = { context ->
                                val themeRes = if (isDark) {
                                    android.R.style.Theme_Material
                                } else {
                                    android.R.style.Theme_Material_Light
                                }
                                LayoutInflater.from(ContextThemeWrapper(context, themeRes))
                                    .inflate(R.layout.date_picker_spinner, null) as DatePicker
                            },
                        )
                    }
                },
            )
        }
    }
}
