package br.com.pedromagno.communication.channel

import br.com.pedromagno.core.Environment
import br.com.pedromagno.event.SimEvent

/**
 * Canal de comunicação FIFO para processos simulados.
 *
 * Serve para modelar UDP, MQTT, LoRa, filas internas, barramentos etc.
 */
class SimChannel<T>(
    private val env: Environment,
    val name: String,
    val capacity: Int = Int.MAX_VALUE,
) {
    private val buffer = ArrayDeque<T>()
    private val waitingReceivers = ArrayDeque<SimEvent<T>>()
    private val waitingSenders = ArrayDeque<PendingSend<T>>()

    init {
        require(capacity > 0) { "A capacidade do canal precisa ser maior que zero."}
    }

    fun send(value: T): SimEvent<Unit>{
        val sendEvent = SimEvent(env.now, Unit)

        when {
            waitingReceivers.isNotEmpty() -> {
                val receiverEvent = waitingReceivers.removeFirst()
                receiverEvent.triggerWith(value)
                env.schedule(sendEvent)
            }

            buffer.size < capacity -> {
                buffer.addLast(value)
                env.schedule(sendEvent)
            }

            else -> {
                waitingSenders.addLast(PendingSend(value, sendEvent))
            }
        }

        return sendEvent
    }

    fun receive(): SimEvent<T> {
        val receiveEvent = SimEvent<T>(env.now)

        if (buffer.isNotEmpty()){
            val value = buffer.removeFirst()
            receiveEvent.triggerWith(value)
            flushWaitingSender()
        }
        else {
            waitingReceivers.addLast(receiveEvent)
        }
        return receiveEvent
    }

    private fun flushWaitingSender() {
        if (waitingSenders.isEmpty()) return
        if (waitingReceivers.isNotEmpty()) {
            val pendingSender = waitingSenders.removeFirst()
            val receiverEvent = waitingReceivers.removeFirst()

            receiverEvent.triggerWith(pendingSender.value)
            pendingSender.ack.trigger()
            return
        }

        if (buffer.size < capacity) {
            val pendingSender = waitingSenders.removeFirst()
            buffer.addLast(pendingSender.value)
            pendingSender.ack.trigger()
        }
    }
    fun size(): Int = buffer.size

    fun waitingReceivers(): Int = waitingReceivers.size
    fun waitingSenders(): Int = waitingSenders.size

    private data class PendingSend<T>(
        val value: T,
        val ack: SimEvent<Unit>
    )
}