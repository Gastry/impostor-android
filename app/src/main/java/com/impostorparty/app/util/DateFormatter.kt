package com.impostorparty.app.util

import java.text.DateFormat
import java.util.Date

fun Long.formatAsShortDateTime(): String {
    val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    return formatter.format(Date(this))
}