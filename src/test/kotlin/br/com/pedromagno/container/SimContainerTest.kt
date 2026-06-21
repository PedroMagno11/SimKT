package br.com.pedromagno.container

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SimContainerTest {

    @Test
    fun `put increases level`() {
        val env = Environment()
        val tank = env.container("tank", capacity = 100.0)

        env.process(SimProcess { ctx ->
            ctx.put(tank, 40.0)
            assertEquals(40.0, tank.level)
        })

        env.run()
    }

    @Test
    fun `get decreases level`() {
        val env = Environment()
        val tank = env.container("tank", capacity = 100.0, initialLevel = 50.0)

        env.process(SimProcess { ctx ->
            ctx.get(tank, 20.0)
            assertEquals(30.0, tank.level)
        })

        env.run()
    }

    @Test
    fun `get blocks when level is insufficient`() {
        val env = Environment()
        val tank = env.container("tank", capacity = 100.0, initialLevel = 0.0)
        val log = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            log.add("waiting at ${ctx.now.value}")
            ctx.get(tank, 50.0)
            log.add("got at ${ctx.now.value}")
        })

        env.process(SimProcess { ctx ->
            ctx.hold(5.0)
            ctx.put(tank, 50.0)
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf("waiting at 0.0", "got at 5.0"), log)
    }

    @Test
    fun `put blocks when container is full`() {
        val env = Environment()
        val tank = env.container("tank", capacity = 50.0, initialLevel = 50.0)
        val log = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            log.add("putting at ${ctx.now.value}")
            ctx.put(tank, 10.0)
            log.add("put done at ${ctx.now.value}")
        })

        env.process(SimProcess { ctx ->
            ctx.hold(3.0)
            ctx.get(tank, 10.0)
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf("putting at 0.0", "put done at 3.0"), log)
    }

    @Test
    fun `negative capacity throws`() {
        val env = Environment()
        assertThrows<IllegalArgumentException> {
            env.container("bad", capacity = -1.0)
        }
    }
}
