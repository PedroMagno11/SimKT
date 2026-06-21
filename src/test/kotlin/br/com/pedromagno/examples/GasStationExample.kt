package br.com.pedromagno.examples

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess

/**
 * Gas station simulation.
 *
 * Cars arrive periodically and queue for one of the pumps (SimResource).
 * Each pump draws from a shared underground fuel tank (SimContainer).
 * A tanker truck refills the tank midway through the simulation.
 *
 * Demonstrates: SimResource (pumps), SimContainer (fuel tank), Simulation class.
 */

const val TANK_CAPACITY = 500.0
const val PUMP_COUNT = 2
const val REFILL_AMOUNT = 300.0
const val REFILL_AT = 50.0
const val SIM_DURATION = 100.0

fun main() {
    val env = Environment()
    val pumps = env.resource("pumps", capacity = PUMP_COUNT)
    val fuelTank = env.container("underground-tank", capacity = TANK_CAPACITY, initialLevel = 300.0)

    fun car(id: Int, arrivalTime: Double, fuelNeeded: Double) = SimProcess { ctx ->
        ctx.hold(arrivalTime)
        ctx.env.monitor.record(ctx.now, "Car $id arrived (needs ${fuelNeeded}L)")

        val pump = ctx.request(pumps)
        ctx.env.monitor.record(ctx.now, "Car $id at pump (tank level: ${fuelTank.level}L)")

        ctx.get(fuelTank, fuelNeeded)
        ctx.hold(fuelNeeded / 20.0)  // 20 L/min fill rate

        ctx.release(pumps, pump)
        ctx.env.monitor.record(ctx.now, "Car $id done (tank now: ${fuelTank.level}L)")
    }

    val tanker = SimProcess { ctx ->
        ctx.hold(REFILL_AT)
        ctx.env.monitor.record(ctx.now, "Tanker arrived — refilling ${REFILL_AMOUNT}L")
        ctx.put(fuelTank, REFILL_AMOUNT)
        ctx.env.monitor.record(ctx.now, "Tanker done (tank now: ${fuelTank.level}L)")
    }

    val arrivals = listOf(
        Triple(1, 0.0, 40.0),
        Triple(2, 2.0, 60.0),
        Triple(3, 5.0, 30.0),
        Triple(4, 8.0, 80.0),
        Triple(5, 30.0, 50.0),
        Triple(6, 55.0, 90.0),
        Triple(7, 60.0, 45.0),
    )

    arrivals.forEach { (id, arrival, fuel) -> env.process(car(id, arrival, fuel)) }
    env.process(tanker)

    env.run(until = SimTime(SIM_DURATION))

    println("=== Gas Station Simulation ===")
    env.monitor.printAll()
    println("Final tank level: ${fuelTank.level}L / ${TANK_CAPACITY}L")
    println("Pumps in use at end: ${pumps.used()}")
}
