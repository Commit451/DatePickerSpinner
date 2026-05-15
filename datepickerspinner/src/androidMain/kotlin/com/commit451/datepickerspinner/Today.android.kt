package com.commit451.datepickerspinner

import java.util.Calendar

actual fun today(): CalendarDate {
    val calendar = Calendar.getInstance()
    return CalendarDate(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
    )
}
