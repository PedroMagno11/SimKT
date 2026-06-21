package br.com.pedromagno.communication

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SimChannelTest {

    @Test
    fun `sender unblocks when receiver is ready`() {
        val env = Environment()
        val channel = env.channel<Int>("ch", capacity = 1)
        val received = mutableListOf<Int>()

        env.process(SimProcess { ctx ->
            ctx.send(channel, 42)
        })

        env.process(SimProcess { ctx ->
            received.add(ctx.receive(channel))
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf(42), received)
    }

    @Test
    fun `receiver blocks until sender sends`() {
        val env = Environment()
        val channel = env.channel<String>("ch")
        val log = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            log.add("waiting at ${ctx.now.value}")
            val msg = ctx.receive(channel)
            log.add("received '$msg' at ${ctx.now.value}")
        })

        env.process(SimProcess { ctx ->
            ctx.hold(5.0)
            ctx.send(channel, "hello")
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf("waiting at 0.0", "received 'hello' at 5.0"), log)
    }

    @Test
    fun `channel buffers up to capacity`() {
        val env = Environment()
        val channel = env.channel<Int>("ch", capacity = 3)

        env.process(SimProcess { ctx ->
            ctx.send(channel, 1)
            ctx.send(channel, 2)
            ctx.send(channel, 3)
        })

        env.run(until = SimTime(1.0))

        assertEquals(3, channel.size())
    }

    @Test
    fun `messages arrive in FIFO order`() {
        val env = Environment()
        val channel = env.channel<Int>("ch", capacity = 5)
        val received = mutableListOf<Int>()

        env.process(SimProcess { ctx ->
            repeat(5) { ctx.send(channel, it) }
        })

        env.process(SimProcess { ctx ->
            repeat(5) { received.add(ctx.receive(channel)) }
        })

        env.run()

        assertEquals(listOf(0, 1, 2, 3, 4), received)
    }
}
