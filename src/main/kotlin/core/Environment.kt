package br.com.pedromagno.core

import java.util.PriorityQueue

/**
 * Esta classe representa o ambiente.
 * Aqui é onde ocorre todas as simulações.
 */
class Environment {
    var now: Double = 0.0
        private set

    private var nextEventId: Long = 0L

    private val eventQueue = PriorityQueue<SimEvent>(
        compareBy <SimEvent> {it.time}
            .thenBy { it.priority }
            .thenBy { it.id }
    )


    /**
     * Este método agenda a execução de uma ação, essa ação
     * será adiciona a uma fila de prioridades e com base
     * no seu delay, prioridade e id, necessariamente nesta ordem, ela será chamada.
     *
     * Este método recebe:
     * - delay (intervalo de tempo para que uma ação seja executada)
     * - priority (prioridade de execução da ação. Por padrão, a nova ação vai para o final da fila.)
     * - action (a ação propriamente dita).
     */
    fun schedule(
        delay: Double,
        priority: Int = eventQueue.size,
        action: Environment.() -> Unit
    ){
        require(delay >= 0.0){
            "O delay não pode ser negativo."
        }

        eventQueue.add(SimEvent(
            id = nextEventId++,
            time = now + delay,
            priority = priority,
            action = action
        ))
    }

    fun run(until: Double = Double.POSITIVE_INFINITY){
        require(until >= now){
            "O tempo final não pode ser menor que o tempo atual."
        }

        while(eventQueue.isNotEmpty()){
            val event: SimEvent = eventQueue.poll()

            if(event.time > until){
                now = until
                break
            }

            now = event.time
            event.action(this)
        }
    }

    fun hasEvent(): Boolean{
        return eventQueue.isNotEmpty()
    }
}
