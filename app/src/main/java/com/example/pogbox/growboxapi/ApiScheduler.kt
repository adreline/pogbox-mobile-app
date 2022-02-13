package com.example.pogbox.growboxapi


import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class ApiScheduler(private val service: GrowboxApi, private val interval: Long = 1000, private val initialDelay: Long? = 0) :
    CoroutineScope {

    private val job = Job()

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()


    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay?.let {
            delay(it)
        }
        while (isActive) {
            service.updateData(service.DHT_URL)
            service.updateData(service.DHT2_URL)
            service.updateData(service.DST_URL)
            service.updateGrowlightState()
            service.updateExhaustState()
            delay(interval)
        }
        println("coroutine done")
    }
}