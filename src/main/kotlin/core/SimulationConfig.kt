package br.com.pedromagno.core

data class SimulationConfig(
    val until: SimTime? = null,
    val maxEvents: Long? = null,
    val randomSeed: Long? = null
)