package br.com.pedromagno.continuous

import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.SimProcess
import br.com.pedromagno.process.SimProcessContext

class SimContinuousRunner(
    private val stepSize: Double,
    private val until: SimTime? = null,
    private val models: List<SimContinuousModel>,
) : SimProcess {
    init {
        require(stepSize > 0.0) { "Step size must be greater than zero." }
    }

    override suspend fun run(context: SimProcessContext) {
        while (until == null || context.now < until) {
            models.forEach { model -> model.step(context, stepSize) }
            context.hold(stepSize)
        }
    }
}
