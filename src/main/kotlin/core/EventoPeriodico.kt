package br.com.pedromagno.core

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class EventoPeriodico(env: Environment, bus: EventBus): SimEntity(env, bus) {

    val timer: Timer = Timer()

    override fun start(){
        timer.schedule(object : TimerTask() {
            override fun run() {
                println("MANDEI")
                bus.emit("event", "Algo aconteceu")
            }
        }, 0, 1000)
    }

    fun stop(){
        timer.cancel()
    }
}