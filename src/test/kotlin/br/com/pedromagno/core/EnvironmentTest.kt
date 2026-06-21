package br.com.pedromagno.core

import br.com.pedromagno.process.SimProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EnvironmentTest {

    @Test
    fun `timeout schedules event at correct time`() {
        val env = Environment()
        var triggered = false

        env.process(SimProcess { ctx ->
            ctx.hold(5.0)
            triggered = true
        })

        env.run(until = SimTime(10.0))

        assertTrue(triggered)
        assertEquals(5.0, env.now.value)
    }

    @Test
    fun `multiple processes interleave in time order`() {
        val env = Environment()
        val log = mutableListOf<Double>()

        env.process(SimProcess { ctx ->
            ctx.hold(3.0)
            log.add(ctx.now.value)
        })

        env.process(SimProcess { ctx ->
            ctx.hold(1.0)
            log.add(ctx.now.value)
            ctx.hold(3.0)
            log.add(ctx.now.value)
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf(1.0, 3.0, 4.0), log)
    }

    @Test
    fun `run stops at maxEvents`() {
        val env = Environment()
        var count = 0

        env.process(SimProcess { ctx ->
            repeat(10) {
                ctx.hold(1.0)
                count++
            }
        })

        env.run(until = null, maxEvents = 3)

        assertEquals(3, count)
    }

    @Test
    fun `reset clears state`() {
        val env = Environment()
        env.process(SimProcess { ctx -> ctx.hold(5.0) })
        env.run(until = SimTime(10.0))

        env.reset()

        assertEquals(SimTime.ZERO, env.now)
    }

    @Test
    fun `stop halts execution mid-run`() {
        val env = Environment()
        var count = 0

        env.process(SimProcess { ctx ->
            repeat(10) {
                ctx.hold(1.0)
                count++
                if (count == 3) ctx.env.stop()
            }
        })

        env.run()

        assertEquals(3, count)
    }

    @Test
    fun `run with SimConfig applies until`() {
        val env = Environment()
        val log = mutableListOf<Double>()

        env.process(SimProcess { ctx ->
            repeat(10) {
                ctx.hold(1.0)
                log.add(ctx.now.value)
            }
        })

        env.run(SimConfig(until = SimTime(5.0)))

        assertEquals(5, log.size)
        assertEquals(5.0, log.last())
    }
}
