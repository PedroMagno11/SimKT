package br.com.pedromagno.resource

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SimResourceTest {

    @Test
    fun `single process acquires and releases resource`() {
        val env = Environment()
        val resource = env.resource("antenna", capacity = 1)
        val log = mutableListOf<String>()

        env.process(SimProcess { ctx ->
            val token = ctx.request(resource)
            log.add("acquired at ${ctx.now.value}")
            ctx.hold(3.0)
            ctx.release(resource, token)
            log.add("released at ${ctx.now.value}")
        })

        env.run(until = SimTime(10.0))

        assertEquals(listOf("acquired at 0.0", "released at 3.0"), log)
        assertEquals(0, resource.used())
    }

    @Test
    fun `second process waits for capacity`() {
        val env = Environment()
        val resource = env.resource("channel", capacity = 1)
        val acquiredAt = mutableListOf<Double>()

        repeat(2) {
            env.process(SimProcess { ctx ->
                val token = ctx.request(resource)
                acquiredAt.add(ctx.now.value)
                ctx.hold(5.0)
                ctx.release(resource, token)
            })
        }

        env.run(until = SimTime(20.0))

        assertEquals(0.0, acquiredAt[0])
        assertEquals(5.0, acquiredAt[1])
    }

    @Test
    fun `resource capacity greater than 1 allows concurrent access`() {
        val env = Environment()
        val resource = env.resource("multi", capacity = 2)
        val acquiredAt = mutableListOf<Double>()

        repeat(2) {
            env.process(SimProcess { ctx ->
                val token = ctx.request(resource)
                acquiredAt.add(ctx.now.value)
                ctx.hold(5.0)
                ctx.release(resource, token)
            })
        }

        env.run(until = SimTime(20.0))

        assertEquals(listOf(0.0, 0.0), acquiredAt)
    }

    @Test
    fun `release with wrong token throws`() {
        val env = Environment()
        val r1 = env.resource("r1", capacity = 1)
        val r2 = env.resource("r2", capacity = 1)

        env.process(SimProcess { ctx ->
            val token = ctx.request(r1)
            assertThrows<IllegalArgumentException> { ctx.release(r2, token) }
            ctx.release(r1, token)
        })

        env.run()
    }

    @Test
    fun `available and used reflect state`() {
        val env = Environment()
        val resource = env.resource("res", capacity = 3)

        env.process(SimProcess { ctx ->
            val t1 = ctx.request(resource)
            assertEquals(2, resource.available())
            assertEquals(1, resource.used())
            val t2 = ctx.request(resource)
            assertEquals(1, resource.available())
            ctx.release(resource, t1)
            ctx.release(resource, t2)
            assertEquals(3, resource.available())
        })

        env.run()
    }
}
