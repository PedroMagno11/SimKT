package br.com.pedromagno.core

import br.com.pedromagno.process.ISimProcess

class Simulation(
    val name: String,
    val env: Environment = Environment()
) {
    private val entities = mutableListOf<SimEntity>()

    fun addEntity(entity: SimEntity): Simulation {
        entities.add(entity)
        entity.onCreate(env)
        return this
    }

    fun addProcess(process: ISimProcess): Simulation {
        env.process(process)
        return this
    }

    fun run(until: Double) {
        env.run(until = SimTime(until))
    }

    fun printReport() {
        println("Simulation: $name")
        println("Time: ${env.now}")
        println("Entities: ${entities.size}")
        env.monitor.printAll()
    }
}