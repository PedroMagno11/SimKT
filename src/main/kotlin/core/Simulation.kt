package br.com.pedromagno.core

class Simulation {
    private val env: Environment = Environment()
    private val bus: EventBus = EventBus()
    private val entities: MutableList<SimEntity> = mutableListOf<SimEntity>()

    private var started: Boolean = false

    fun add(entityFactory: (Environment, EventBus) -> SimEntity): Simulation {
        val entity = entityFactory(env, bus)
        entities.add(entity)
        return this
    }

    fun start(){
        if (started) return

        entities.forEach {
            entity -> entity.start()
        }

        started = true
    }

    fun run(until: Double = Double.POSITIVE_INFINITY) {
        start()
        env.run(until)
    }
}