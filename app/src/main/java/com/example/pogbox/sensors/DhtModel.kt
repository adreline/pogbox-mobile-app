package com.example.pogbox.sensors

import java.io.Serializable


data class DhtModel(
    val temperature: Float,
    val humidity: Float,
    val time_stamp: String
) : Serializable
