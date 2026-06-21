package br.com.pedromagno.core

import br.com.pedromagno.communication.channel.SimChannel
import br.com.pedromagno.communication.queue.SimQueue
import br.com.pedromagno.container.SimContainer
import br.com.pedromagno.continuous.SimContinuousModel
import br.com.pedromagno.continuous.SimContinuousRunner
import br.com.pedromagno.event.SimEvent
import br.com.pedromagno.monitor.SimMonitor
import br.com.pedromagno.process.SimProcess
import br.com.pedromagno.process.SimProcessContext
import br.com.pedromagno.random.SimRandomProvider
import br.com.pedromagno.resource.SimResource
import br.com.pedromagno.schedule.PriorityQueueScheduler
import br.com.pedromagno.schedule.SimScheduler
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class Environment(
    private val clock: SimClock = SimClock(),
    private val scheduler: SimScheduler = PriorityQueueScheduler(),
    val monitor: SimMonitor = SimMonitor(),
    val random: SimRandomProvider = SimRandomProvider(),
) {
    val now: SimTime
        get() = clock.now

    private var running = false
    private var processCounter = 0L

    fun timeout(delay: Double, action: () -> Unit = {}): SimEvent<Unit> {
        require(delay >= 0.0) { "Delay must not be negative." }
        val event = SimEvent(time = now + delay, value = Unit, action = action)
        schedule(event)
        return event
    }

    fun timeout(delay: Long, action: () -> Unit = {}): SimEvent<Unit> = timeout(delay.toDouble(), action)

    fun <T> eventAt(time: SimTime, value: T? = null, action: (() -> Unit)? = null): SimEvent<T> {
        val event = SimEvent(time = time, value = value, action = action)
        schedule(event)
        return event
    }

    fun <T> eventNow(value: T? = null, action: (() -> Unit)? = null): SimEvent<T> = eventAt(now, value, action)

    fun schedule(event: SimEvent<*>) {
        require(event.time >= now) {
            "Cannot schedule an event in the past. Now: $now, event: ${event.time}"
        }
        scheduler.schedule(event)
    }

    fun process(process: SimProcess) {
        val context = SimProcessContext(this)
        val processId = ++processCounter

        process::run.startCoroutine(
            context,
            Continuation(EmptyCoroutineContext) { result ->
                result.exceptionOrNull()?.let { error ->
                    monitor.record(now, "Process #$processId failed: ${error.message}")
                    throw error
                }
            }
        )
    }

    fun resource(name: String, capacity: Int): SimResource =
        SimResource(name = name, capacity = capacity, env = this)

    fun <T> channel(name: String, capacity: Int = Int.MAX_VALUE): SimChannel<T> =
        SimChannel(env = this, name = name, capacity = capacity)

    fun <T> store(name: String, capacity: Int = Int.MAX_VALUE): SimQueue<T> =
        SimQueue(env = this, name = name, capacity = capacity)

    fun container(name: String, capacity: Double, initialLevel: Double = 0.0): SimContainer =
        SimContainer(env = this, name = name, capacity = capacity, initialLevel = initialLevel)

    fun continuous(stepSize: Double, until: SimTime? = null, vararg models: SimContinuousModel) {
        process(SimContinuousRunner(stepSize = stepSize, until = until, models = models.toList()))
    }

    fun run(until: SimTime? = null, maxEvents: Long? = null) {
        running = true
        var executedEvents = 0L

        while (running && scheduler.hasNext()) {
            if (maxEvents != null && executedEvents >= maxEvents) break
            val event = scheduler.next() ?: break
            if (until != null && event.time > until) break
            clock.advanceTo(event.time)
            event.trigger()
            executedEvents++
        }
    }

    fun run(config: SimConfig) {
        if (config.randomSeed != null) random.reseed(config.randomSeed)
        run(until = config.until, maxEvents = config.maxEvents)
    }

    fun stop() {
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
