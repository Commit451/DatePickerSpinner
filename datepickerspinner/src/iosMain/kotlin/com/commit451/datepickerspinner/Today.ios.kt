package com.commit451.datepickerspinner

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun today(): CalendarDate {
    val components = NSCalendar.currentCalendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        NSDate(),
    )
    return CalendarDate(
        year = components.year.toInt(),
        month = components.month.toInt(),
        dayOfMonth = components.day.toInt(),
    )
}
