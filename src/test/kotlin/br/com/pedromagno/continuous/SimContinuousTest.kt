package br.com.pedromagno.continuous

import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SimContinuousTest {

    @Test
    fun `continuous model steps at fixed intervals`() {
        val env = Environment()
        var position = 0.0
        val speed = 10.0
        val steps = mutableListOf<Double>()

        env.continuous(
            stepSize = 1.0,
            until = SimTime(5.0),
            SimContinuousModel { _, dt ->
                position += speed * dt
                steps.add(position)
            }
        )

        env.run(until = SimTime(10.0))

        assertEquals(5, steps.size)
        assertEquals(50.0, position, 1e-9)
    }

    @Test
    fun `continuous model stops at until time`() {
        val env = Environment()
        var stepCount = 0

        env.continuous(
            stepSize = 1.0,
            until = SimTime(3.0),
            SimContinuousModel { _, _ -> stepCount++ }
        )

        env.run(until = SimTime(100.0))

        assertEquals(3, stepCount)
    }

    @Test
    fun `multiple continuous models run each step`() {
        val env = Environment()
        val modelALog = mutableListOf<Double>()
        val modelBLog = mutableListOf<Double>()

        env.continuous(
            stepSize = 1.0,
            until = SimTime(3.0),
            SimContinuousModel { ctx, _ -> modelALog.add(ctx.now.value) },
            SimContinuousModel { ctx, _ -> modelBLog.add(ctx.now.value) }
        )

        env.run(until = SimTime(10.0))

        assertEquals(modelALog, modelBLog)
        assertEquals(3, modelALog.size)
    }
}
