package br.com.pedromagno.container

import br.com.pedromagno.core.Environment
import br.com.pedromagno.event.SimEvent

/**
 * Container numérico para modelar
 * combustível, bateria, água, carga,
 * estoque contínuo etc.
 */
class SimContainer(
    private val env: Environment,
    val name: String,
    val capacity: Double,
    initialLevel: Double = 0.0
) {
    var level: Double = initialLevel
        private set

    private val waitingGets = ArrayDeque<PendingAmount>()
    private val waitingPuts = ArrayDeque<PendingAmount>()

    init {
        require(capacity > 0.0) {
            "A capacidade do container preceisa ser maior que zero."
        }
        require(initialLevel in 0.0 .. capacity){
            "O nível inicial precisa estar entre 0 e a capacidade."
        }
    }

    fun get(amount: Double): SimEvent<Unit>{
        require(amount > 0.0){
            "A quantidade precisa ser maior que zero."
        }

        val event = SimEvent(env.now, Unit)

        if (level >= amount){
            level -= amount
            env.schedule(event)
            flushWaitingOperations()
        } else {
            waitingGets.addLast(PendingAmount(amount, event))
        }

        return event
    }

    fun put(amout: Double): SimEvent<Unit> {
        require(amout > 0.0){"A quantidade precisa ser maior que zero."}

        val event = SimEvent(env.now, Unit)

        if (available() >= amout){
            level += amout
            env.schedule(event)
            flushWaitingOperations()
        } else {
            waitingPuts.addLast(PendingAmount(amout, event))
        }
        return event
    }

    fun available():  Double = capacity - level
    private fun flushWaitingOperations(){
        var changed: Boolean

        do {
            changed = false

            while (waitingGets.isNotEmpty() && level >= waitingGets.first().amount){
                val pending = waitingGets.removeFirst()
                level -= pending.amount
                pending.event.trigger()
                changed = true
            }

            while (waitingPuts.isNotEmpty() && available() >= waitingGets.first().amount){
                val pending = waitingPuts.removeFirst()
                level += pending.amount
                pending.event.trigger()
                changed = true
            }
        } while (changed)
    }

    override fun toString(): String {
        return "SimContainer(name='$name', capacity=$capacity, level=$level)"
    }
    private data class PendingAmount(
        val amount: Double,
        val event: SimEvent<Unit>
    )
}