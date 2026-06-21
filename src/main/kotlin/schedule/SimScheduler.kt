package br.com.pedromagno.schedule

import br.com.pedromagno.event.SimEvent

interface SimScheduler {
    fun schedule(event: SimEvent<*>)
    fun next(): SimEvent<*>?
    fun hasNext(): Boolean
    fun clear()
}
