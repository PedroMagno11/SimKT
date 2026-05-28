package br.com.pedromagno.core

/**
 * Esta classe é usada para simular um evento.
 */
data class SimEvent(
    val id: Long,
    val time: Double,
    val priority: Int,
    val action: Environment.() -> Unit
    )