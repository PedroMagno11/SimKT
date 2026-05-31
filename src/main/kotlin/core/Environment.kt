package br.com.pedromagno.core

import br.com.pedromagno.communication.channel.SimChannel
import br.com.pedromagno.container.SimContainer
import br.com.pedromagno.continuous.ContinuousRunner
import br.com.pedromagno.continuous.SimContinuousModel
import br.com.pedromagno.event.SimEvent
import br.com.pedromagno.monitor.SimulationMonitor
import br.com.pedromagno.process.ISimProcess
import br.com.pedromagno.process.SimProcessContext
import br.com.pedromagno.random.SimRandomProvider
import br.com.pedromagno.resource.SimResource
import br.com.pedromagno.schedule.ISimScheduler
import br.com.pedromagno.schedule.PriorityQueueScheduler
import br.com.pedromagno.store.SimQueue
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Esta classe representa o ambiente central da simulação.
 *
 * Responsabilidade:
 * - manter o tempo simulado
 * - agendar e executar eventos discretos
 * - iniciar processos suspensos
 * - criar recursos, canais, stores e containers associados ao ambiente.
 */
class Environment(
    private val clock: SimClock = SimClock(),
    private val scheduler: ISimScheduler = PriorityQueueScheduler(),
    val monitor: SimulationMonitor = SimulationMonitor(),
    val random: SimRandomProvider = SimRandomProvider(),
){
    val now: SimTime
        get() = clock.now

    private var running = false
    private var processCounter = 0L
    fun timeout(delay: Double, action: () -> Unit = {}): SimEvent<Unit> {
        require(delay >= 0.0) {
            "O Delay não deve ser negativo."
        }

        val event = SimEvent(
            time = now + delay,
            value = Unit,
            action = action,
        )

        schedule(event)
        return event
    }

    fun timeout(delay: Long, action: () -> Unit = {}): SimEvent<Unit>{
        return timeout(delay.toDouble(), action)
    }

    fun <T> eventAt(
        time: SimTime,
        value: T? = null,
        action: (() -> Unit)? = null
    ): SimEvent<T> {
        val event = SimEvent(
            time = time,
            value = value,
            action = action,
        )

        schedule(event)
        return event
    }

    fun <T> eventNow(value: T? = null, action:(() -> Unit)? = null): SimEvent<T>{
        return eventAt(now, value, action)
    }

    fun schedule(event: SimEvent<*>) {
        require(event.time >= now) {
            "Não é possível agendar um evento no passado. Agora: $now, evento: ${event.time}"
        }
        scheduler.schedule(event)
    }

    /**
     * Inicia um processo sem bloquear a thread real.
     *
     * A coroutine executa até o primeiro ponto de suspensão,
     * como hold(), await(), request(), receive() etc. Depois
     * disso, ela só continua quando um SimEvent for disparado pelo
     * scheduler durante env.run().
     */
    fun process(process: ISimProcess){
        val context = SimProcessContext(this)
        val processId = ++processCounter

        process::run.startCoroutine(
            context,
            Continuation(EmptyCoroutineContext){ result ->
                result.exceptionOrNull()?.let {
                    error -> monitor.record(now, "Processo #$processId falhou: ${error.message}")
                    throw error
                }
            }
        )
    }

    fun resource(name: String, capacity: Int): SimResource {
        return SimResource(
            name = name,
            capacity = capacity,
            env = this,
        )
    }

    fun <T> channel(name: String, capacity: Int = Int.MAX_VALUE): SimChannel<T> {
        return SimChannel(env = this, name = name, capacity = capacity)
    }

    fun <T> store(name: String, capacity: Int = Int.MAX_VALUE) : SimQueue<T> {
        return SimQueue(env = this, name = name, capacity = capacity)
    }

    fun container(name: String, capacity: Double, initialLevel: Double = 0.0): SimContainer {
        return SimContainer(env = this, name = name, capacity = capacity, initialLevel = initialLevel)
    }

    fun continuous(
        stepSize: Double,
        until: SimTime? = null,
        vararg models: SimContinuousModel,
    ){
        process(
            ContinuousRunner(
                stepSize = stepSize,
                until = until,
                models = models.toList()
            )
        )
    }
    fun run(until: SimTime? = null, maxEvents: Long? = null) {
        running = true
        var executedEvents: Long = 0L

        while (running && scheduler.hasNext()) {
            if (maxEvents != null && executedEvents >= maxEvents) break

            val event = scheduler.next() ?: break

            if (until != null && event.time > until){
                break
            }

            clock.advanceTo(event.time)
            event.trigger()
            executedEvents++
        }
    }

    fun run(config: SimulationConfig){
        run(
            until = config.until,
            maxEvents = config.maxEvents,
        )
    }

    fun stop(){
        running = false
    }

    fun reset() {
        running = false
        processCounter = 0L
        clock.reset()
        scheduler.clear()
        monitor.clear()
    }
}
