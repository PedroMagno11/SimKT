package br.com.pedromagno.event

import br.com.pedromagno.core.SimTime
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Evento discreto da simulação.
 *
 * Um evento possui:
 * - Tempo simulado de ocorrência
 * - Valor opcional de retorno
 * - ação opcional
 * - continuations de processos suspensos aguardando o evento.
 */
class SimEvent<T>(
    val time: SimTime,
    private var value: T? = null,
    private val action: (() -> Unit)? = null
){
    var status: SimEventStatus = SimEventStatus.PENDING
        private set

    private val continuations = mutableListOf<Continuation<T?>>()

    val isPending: Boolean
        get() = status == SimEventStatus.PENDING

    val isTriggered: Boolean
        get() = status == SimEventStatus.TRIGGERED

    val isCancelled: Boolean
        get() = status == SimEventStatus.CANCELLED

    fun valueOrNull(): T? = value

    fun withValue(newValue: T): SimEvent<T> {
        require(status == SimEventStatus.PENDING){
            "Não é possível alterar o valor de um evento que não está pendente."
        }
        value = newValue
        return this
    }
    fun await(continuation: Continuation<T?>) {
        if (status == SimEventStatus.TRIGGERED){
            continuation.resume(value)
        } else if (status == SimEventStatus.CANCELLED) {
            continuation.resume(null)
        } else {
            continuations.add(continuation)
        }
    }

    fun trigger() {
        if(status != SimEventStatus.PENDING) return

        status = SimEventStatus.TRIGGERED
        action?.invoke()
        continuations.forEach { it.resume(value) }
        continuations.clear()
    }

    fun triggerWith(newValue: T) {
        withValue(newValue)
        trigger()
    }

    fun cancel(){
        if(status == SimEventStatus.TRIGGERED) return

        status = SimEventStatus.CANCELLED
        continuations.forEach { it.resume(null) }
        continuations.clear()
    }

    override fun toString(): String {
        return "SimEvent(time=$time, status='$status')"
    }
}