package br.com.pedromagno.examples

import br.com.pedromagno.core.Environment
import br.com.pedromagno.process.ISimProcess

data class Message(
    val id: Int,
    val payload: String
)

fun main(){
    val env = Environment()
    val channel = env.channel<Message>(name = "radio", capacity = 2)

    env.process ( ISimProcess { ctx ->
        ctx.hold(1.0)
        ctx.send(channel, Message(1, "posição GPS"))

        ctx.env.monitor.record(
            ctx.now, "Produtor enviou mensagem"
        )
    })

    env.process(ISimProcess { ctx ->
        val message = ctx.receive(channel)
        ctx.env.monitor.record(ctx.now, "Consumidor recebeu: $message")
    })

    env.run()
    env.monitor.printAll()
}