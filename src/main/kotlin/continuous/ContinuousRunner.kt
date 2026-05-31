package br.com.pedromagno.continuous

import br.com.pedromagno.core.SimTime
import br.com.pedromagno.process.ISimProcess
import br.com.pedromagno.process.SimProcessContext

class ContinuousRunner(
    private val stepSize: Double,
    private val until: SimTime? = null,
    private val models: List<SimContinuousModel>,
): ISimProcess {
    init{
        require(stepSize > 0.0) { "O passo precisa ser maior que zero." }
    }

    override suspend fun run(context: SimProcessContext) {
        while (until == null || context.now < until) {
            models.forEach { model -> model.step(context, stepSize) }
            context.hold(stepSize)
        }
    }
}