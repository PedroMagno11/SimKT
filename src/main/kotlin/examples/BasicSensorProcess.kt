package br.com.pedromagno.examples

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.ISimProcess
import br.com.pedromagno.process.SimProcessContext

class BasicSensorProcess : ISimProcess {
    override suspend fun run(context: SimProcessContext) {
        context.env.monitor.record(context.now, "Sensor iniciado")

        context.hold(5.0)
        context.env.monitor.record(context.now, "Sensor fez a primeira leitura")

        context.hold(5.0)
        context.env.monitor.record(context.now, "Sensor detectou alvo")
    }
}

fun main(){
    val env = Environment()
    env.process(BasicSensorProcess())
    env.run(until = SimTime(20.0))

    env.monitor.printAll()
}