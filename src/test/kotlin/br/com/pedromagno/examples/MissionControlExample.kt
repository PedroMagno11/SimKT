package br.com.pedromagno.examples

import br.com.pedromagno.communication.channel.SimChannel
import br.com.pedromagno.core.SimEntity
import br.com.pedromagno.core.Simulation
import br.com.pedromagno.process.SimProcess
import br.com.pedromagno.resource.SimResource

/**
 * Mission control with multiple UAVs sharing a communication link.
 *
 * Each UAV is modelled as a SimEntity. All UAVs share a single satellite
 * uplink (SimResource with capacity 1) and report telemetry through a
 * SimChannel read by a ground station process.
 *
 * Demonstrates: SimEntity (UAV), Simulation helper class, SimResource
 * (uplink), SimChannel (telemetry stream).
 */

data class Telemetry(val uavId: String, val altitude: Double, val fuel: Double)

class UAV(
    name: String,
    val missionDuration: Double,
    val reportInterval: Double,
) : SimEntity(name = name) {
    val totalReports: Int get() = (missionDuration / reportInterval).toInt()

    fun process(uplink: SimResource,
                telemetryChannel: SimChannel<Telemetry>
    ) = SimProcess { ctx ->
        var altitude = 1000.0
        var fuel = 100.0
        var elapsed = 0.0

        while (elapsed < missionDuration && fuel > 0.0) {
            ctx.hold(reportInterval)
            elapsed += reportInterval
            altitude += ctx.env.random.nextDouble(-50.0, 50.0)
            fuel -= reportInterval * 1.5

            val token = ctx.request(uplink)
            ctx.send(telemetryChannel, Telemetry(name, altitude, maxOf(fuel, 0.0)))
            ctx.release(uplink, token)
        }

        ctx.env.monitor.record(ctx.now, "$name mission complete (fuel: ${"%.1f".format(maxOf(fuel, 0.0))}%)")
    }
}

fun main() {
    val sim = Simulation("UAV Mission Control")
    val env = sim.env

    val uplink = env.resource("satellite-uplink", capacity = 1)
    val telemetry = env.channel<Telemetry>("telemetry", capacity = 20)

    val uavs = listOf(
        UAV("Alpha", missionDuration = 40.0, reportInterval = 5.0),
        UAV("Bravo", missionDuration = 30.0, reportInterval = 8.0),
        UAV("Charlie", missionDuration = 50.0, reportInterval = 6.0),
    )

    uavs.forEach { uav ->
        sim.addEntity(uav)
        sim.addProcess(uav.process(uplink, telemetry))
    }

    val groundStation = SimProcess { ctx ->
        var count = 0
        while (true) {
            val t = ctx.receive(telemetry)
            count++
            ctx.env.monitor.record(ctx.now, "GS received from ${t.uavId}: alt=${"%.0f".format(t.altitude)}m fuel=${"%.1f".format(t.fuel)}%")
            if (count >= uavs.sumOf { uav -> (uav.totalReports) }) break
        }
    }

    sim.addProcess(groundStation)
    sim.run(until = 60.0)
    sim.printReport()
}
