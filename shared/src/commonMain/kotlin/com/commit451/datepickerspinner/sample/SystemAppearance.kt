package com.commit451.datepickerspinner.sample

import androidx.compose.runtime.Composable

/**
 * Applies system-bar appearance for the given theme — most notably the status/navigation bar
 * icon contrast (light icons on dark themes, dark icons on light themes).
 *
 * No-op on platforms that have no system status bar.
 */
@Composable
expect fun SystemAppearance(isDark: Boolean)
