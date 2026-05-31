package br.com.pedromagno.store

import br.com.pedromagno.core.Environment
import br.com.pedromagno.event.SimEvent

class SimQueue<T>(
    private val env: Environment,
    val name: String,
    val capacity: Int = Int.MAX_VALUE,
) {
    private val items = ArrayDeque<T>()
    private val waitingGets = ArrayDeque<SimEvent<T>>()
    private val waitingPuts = ArrayDeque<PendingPut<T>>()

    init {
        require(capacity > 0){ "A capacidade de store precisa ser maior que zero."}
    }

    fun put(item: T): SimEvent<Unit> {
        val putEvent = SimEvent(time = env.now, Unit)

        when{
            waitingGets.isNotEmpty() -> {
                val getEvent = waitingGets.removeFirst()
                getEvent.triggerWith(item)
                env.schedule(putEvent)
            }

            items.size < capacity -> {
                items.addLast(item)
                env.schedule(putEvent)
            }

            else -> {
                waitingPuts.addLast(PendingPut(item, putEvent))
            }
        }
        return putEvent
    }

    fun get(): SimEvent<T> {
        val getEvent = SimEvent<T>(env.now)

        if (items.isNotEmpty()){
            val item = items.removeFirst()
            env.schedule(getEvent.withValue(item))
            flushWaitingPut()
        } else {
            waitingGets.addLast(getEvent)
        }
        return getEvent
    }
    private fun flushWaitingPut() {
        if(waitingPuts.isEmpty()) return

        if (waitingGets.isNotEmpty()){
            val pendingPut = waitingPuts.removeFirst()
            val getEvent = waitingGets.removeFirst()

            getEvent.triggerWith(pendingPut.item)
            pendingPut.ack.trigger()
            return
        }

        if (items.size < capacity) {
            val pendingPut = waitingPuts.removeFirst()
            items.addLast(pendingPut.item)
            pendingPut.ack.trigger()
        }
    }

    fun size(): Int = items.size

    fun waitingGets() : Int = waitingGets.size
    fun waitingPuts() : Int = waitingPuts.size

    private data class PendingPut<T>(
        val item: T,
        val ack: SimEvent<Unit>
    )
}