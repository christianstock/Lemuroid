package com.swordfish.lemuroid.app.mobile.shared

import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

fun formatLocalizedDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return runCatching {
        val cleanDate = dateString.replace("-", "").trim()
        if (cleanDate.length >= 8) {
            val year = cleanDate.substring(0, 4).toInt()
            val month = cleanDate.substring(4, 6).toInt() - 1
            val day = cleanDate.substring(6, 8).toInt()
            val cal = Calendar.getInstance().apply {
                set(year, month, day)
            }
            DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(cal.time)
        } else {
            dateString
        }
    }.getOrDefault(dateString)
}
