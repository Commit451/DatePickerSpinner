package com.commit451.datepickerspinner.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(
                nativeDatePicker = {
                    AndroidView(
                        factory = { context ->
                            LayoutInflater.from(context)
                                .inflate(R.layout.date_picker_spinner, null) as DatePicker
                        },
                    )
                },
            )
        }
    }
}
