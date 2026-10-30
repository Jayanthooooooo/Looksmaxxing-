package com.example.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface SystemClock {
    fun currentTimeMillis(): Long
    fun currentDateString(): String
}

class DefaultSystemClock : SystemClock {
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun currentDateString(): String {
        return dateFormat.format(Date(currentTimeMillis()))
    }
}
