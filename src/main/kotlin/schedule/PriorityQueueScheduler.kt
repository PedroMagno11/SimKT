package br.com.pedromagno.schedule

import br.com.pedromagno.event.SimEvent
import br.com.pedromagno.event.SimScheduledEvent
import java.util.PriorityQueue

class PriorityQueueScheduler : ISimScheduler {

    private val eventsQueue = PriorityQueue<SimScheduledEvent>()
    private var sequenceCounter: Long = 0L

    override fun schedule(event: SimEvent<*>) {
        eventsQueue.add(
            SimScheduledEvent(
                event=event,
                sequence = sequenceCounter++
            )
        )
    }

    override fun next(): SimEvent<*>? {
        while (eventsQueue.isNotEmpty()) {
            val scheduledEvent = eventsQueue.poll()
            val event = scheduledEvent.event

            if(!event.isCancelled){
                return event
            }
        }
        return null
    }

    override fun hasNext(): Boolean {
        return eventsQueue.any{
            !it.event.isCancelled
        }
    }

    override fun clear() {
        eventsQueue.clear()
        sequenceCounter = 0L
    }
}