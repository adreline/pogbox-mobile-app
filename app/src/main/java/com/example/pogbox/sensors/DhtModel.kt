package com.example.pogbox.sensors

import java.sql.Date
import java.time.LocalDateTime

data class DhtModel(
    val temperature: Float,
    val humidity: Float,
    val time_stamp: String

)
