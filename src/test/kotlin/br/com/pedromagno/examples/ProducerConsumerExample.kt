package br.com.pedromagno.examples

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess

/**
 * Producer-consumer pipeline with a bounded buffer.
 *
 * Two producers generate data packets at different rates and push them into
 * a shared SimQueue. Three consumers pull from the same queue and process
 * each packet. The queue capacity limits how many unprocessed packets can
 * accumulate, causing producers to block when it is full.
 *
 * Demonstrates: SimQueue with bounded capacity, multiple producers and consumers.
 */

data class DataPacket(val producerId: Int, val packetId: Int, val size: Int)

fun main() {
    val env = Environment()
    val buffer = env.store<DataPacket>("buffer", capacity = 5)

    fun producer(id: Int, interval: Double, packetSize: Int) = SimProcess { ctx ->
        var packetId = 0
        while (true) {
            ctx.hold(interval)
            val packet = DataPacket(id, ++packetId, packetSize)
            ctx.env.monitor.record(ctx.now, "Producer $id sent packet #${packet.packetId} (buffer: ${buffer.size()}/${buffer.capacity})")
            ctx.put(buffer, packet)
        }
    }

    fun consumer(id: Int, processingTime: Double) = SimProcess { ctx ->
        while (true) {
            val packet = ctx.get(buffer)
            ctx.env.monitor.record(ctx.now, "Consumer $id processing packet #${packet.packetId} from producer ${packet.producerId}")
            ctx.hold(processingTime)
            ctx.env.monitor.record(ctx.now, "Consumer $id done with packet #${packet.packetId}")
        }
    }

    env.process(producer(id = 1, interval = 2.0, packetSize = 100))
    env.process(producer(id = 2, interval = 3.0, packetSize = 200))

    env.process(consumer(id = 1, processingTime = 4.0))
    env.process(consumer(id = 2, processingTime = 6.0))
    env.process(consumer(id = 3, processingTime = 5.0))

    env.run(until = SimTime(30.0))

    println("=== Producer-Consumer Simulation ===")
    env.monitor.printAll()
    println("Packets remaining in buffer: ${buffer.size()}")
    println("Consumers waiting: ${buffer.waitingGets()}")
}
