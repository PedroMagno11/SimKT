package br.com.pedromagno.resource

import br.com.pedromagno.core.Environment
import br.com.pedromagno.event.SimEvent
import kotlin.concurrent.timer

class SimResource(
    private val env: Environment,
    val name: String,
    val capacity: Int,
) {
    private var inUse = 0
    private var requestCounter = 0L

    private val activeRequests = mutableSetOf<Long>()
    private val waitingQueue = ArrayDeque<SimEvent<SimResourceRequest>>()

    init{
        require(capacity > 0) {
            "A capacidade do recurso precisa ser maior que zero."
        }
    }

    fun request(): SimEvent<SimResourceRequest> {
        val requestId = ++ requestCounter
        val token = SimResourceRequest(resourceName = name, id = requestId)

        val event = SimEvent(
            time = env.now,
            value = token,
        )

        if (inUse < capacity){
            inUse++
            activeRequests.add(requestId)
            env.schedule(event)
        } else {
            waitingQueue.addLast(event)
        }

        return event
    }

    fun release(request: SimResourceRequest){

        require(request.resourceName == name) {
            "O token pertence ao recurso ${request.resourceName}, não ao recurso $name."
        }

        require(activeRequests.remove(request.id)){
            "Token de recurso inválido ou já liberado: $request"
        }

        inUse--
        wakeNextWaitingRequest()
    }

    private fun wakeNextWaitingRequest(){
        if(waitingQueue.isEmpty()) return

        val nextEvent = waitingQueue.removeFirst()

        if (nextEvent.isCancelled){
            wakeNextWaitingRequest()
            return
        }

        inUse++
        nextEvent.valueOrNull()?.let{ activeRequests.add(it.id)}
        nextEvent.trigger()
    }

    fun available(): Int = capacity - inUse

    fun used() : Int = inUse

    fun waiting() : Int = waitingQueue.size

    override fun toString(): String {
        return "SimResource(name=$name, capacity=$capacity, inUse=$inUse, waiting=${waitingQueue.size})"
    }
}