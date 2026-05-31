package br.com.pedromagno.examples

import br.com.pedromagno.continuous.SimContinuousModel
import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime

fun main(){
    val env = Environment()
    var position = 0.0
    val speed = 2.0

    env.continuous(
        stepSize = 1.0,
        until = SimTime(5.0),
        SimContinuousModel { ctx, dt ->
            position += speed * dt
            ctx.env.monitor.record(env.now, "posição=$position")
        }
    )

    env.run(until = SimTime(10.0))
    env.monitor.printAll()
}