package br.com.pedromagno.examples

import br.com.pedromagno.continuous.SimContinuousModel
import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess

/**
 * Battery-powered sensor device.
 *
 * A continuous model drains the battery at a rate that increases under heavy
 * load. A discrete process takes periodic sensor readings and changes the
 * device into a low-power mode below 20 % charge, which reduces drain.
 * The simulation ends when the battery runs out.
 *
 * Demonstrates: SimContainer (battery) + SimContinuousModel (discharge) +
 * SimProcess (application logic) working together.
 */

const val BATTERY_CAPACITY = 100.0
const val READING_INTERVAL = 5.0
const val NORMAL_DRAIN_RATE = 3.0  // % per time unit
const val LOW_POWER_DRAIN_RATE = 1.0

fun main() {
    val env = Environment()
    val battery = env.container("battery", capacity = BATTERY_CAPACITY, initialLevel = BATTERY_CAPACITY)

    var lowPowerMode = false
    var readingCount = 0

    val drainModel = SimContinuousModel { ctx, dt ->
        val drain = (if (lowPowerMode) LOW_POWER_DRAIN_RATE else NORMAL_DRAIN_RATE) * dt
        val actual = minOf(drain, battery.level)
        if (actual > 0.0) {
            battery.get(actual)
            if (battery.level <= 0.0) ctx.env.stop()
        }
    }

    val sensorProcess = SimProcess { ctx ->
        while (battery.level > 0.0) {
            ctx.hold(READING_INTERVAL)

            if (battery.level < 0.01) break

            readingCount++
            val pct = battery.level / BATTERY_CAPACITY * 100

            if (!lowPowerMode && pct < 20.0) {
                lowPowerMode = true
                ctx.env.monitor.record(ctx.now, "Entering low-power mode (battery: ${"%.1f".format(pct)}%)")
            }

            ctx.env.monitor.record(ctx.now, "Reading #$readingCount — battery: ${"%.1f".format(pct)}% [${if (lowPowerMode) "LOW-POWER" else "NORMAL"}]")
        }
    }

    env.continuous(stepSize = 1.0, models = arrayOf(drainModel))
    env.process(sensorProcess)

    env.run(until = SimTime(200.0))

    println("=== Battery Device Simulation ===")
    env.monitor.printAll()
    println("Device stopped at t=${env.now.value} with battery at ${"%.2f".format(battery.level)}%")
    println("Total readings taken: $readingCount")
}
