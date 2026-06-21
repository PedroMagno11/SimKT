package br.com.pedromagno.core

data class SimConfig(
    val until: SimTime? = null,
    val maxEvents: Long? = null,
    val randomSeed: Long? = null
)
