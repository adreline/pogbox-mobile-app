package com.example.pogbox.sensors

import java.io.Serializable
import java.sql.Date

data class DstModel(
    val temperature: Float,
    val time_stamp: String
) : Serializable
