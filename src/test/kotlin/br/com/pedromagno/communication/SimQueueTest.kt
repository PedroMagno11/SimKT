package br.com.pedromagno.communication

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SimQueueTest {

    @Test
    fun `put and get in order`() {
        val env = Environment()
        val store = env.store<String>("store")
        val result = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            ctx.put(store, "a")
            ctx.put(store, "b")
            ctx.put(store, "c")
            result.add(ctx.get(store))
            result.add(ctx.get(store))
            result.add(ctx.get(store))
        })

        env.run()

        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `get blocks when store is empty`() {
        val env = Environment()
        val store = env.store<Int>("store")
        val log = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            log.add("getting at ${ctx.now.value}")
            val v = ctx.get(store)
            log.add("got $v at ${ctx.now.value}")
        })

        env.process(SimProcess { ctx ->
            ctx.hold(3.0)
            ctx.put(store, 99)
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf("getting at 0.0", "got 99 at 3.0"), log)
    }

    @Test
    fun `put blocks when store is full`() {
        val env = Environment()
        val store = env.store<Int>("store", capacity = 1)
        val log = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            ctx.put(store, 1)
            log.add("put 1 at ${ctx.now.value}")
            ctx.put(store, 2)
            log.add("put 2 at ${ctx.now.value}")
        })

        env.process(SimProcess { ctx ->
            ctx.hold(5.0)
            ctx.get(store)
        })

        env.run(until = SimTime(10.0))

        assertEquals("put 1 at 0.0", log[0])
        assertEquals("put 2 at 5.0", log[1])
    }
}
