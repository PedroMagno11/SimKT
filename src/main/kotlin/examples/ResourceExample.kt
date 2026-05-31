package br.com.pedromagno.examples

import br.com.pedromagno.core.Environment
import br.com.pedromagno.process.ISimProcess

fun main(){
    val env = Environment()
    val antenna = env.resource(name = "antena", capacity = 1)

    repeat(3){ index ->
        env.process(ISimProcess {ctx ->
            ctx.env.monitor.record(ctx.now, "Mensagem $index aguardando antena")
            val token = ctx.request(antenna)
            ctx.env.monitor.record(ctx.now, "Mensagem $index usando antena")
            ctx.hold(2.0)
            ctx.release(antenna, token)
            ctx.env.monitor.record(ctx.now, "Mensagem $index liberou antena")
        })
    }

    env.run()
    env.monitor.printAll()
}