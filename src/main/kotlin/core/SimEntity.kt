package br.com.pedromagno.core

abstract class SimEntity(
    protected val env: Environment,
    protected val bus: EventBus
) {
    abstract fun start()
}