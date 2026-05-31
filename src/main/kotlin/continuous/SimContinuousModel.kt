package br.com.pedromagno.continuous

import br.com.pedromagno.process.SimProcessContext

/**
 * Modelo contínuo executado em passos fixos dentro do motor de eventos
 * discretos.
 *
 * Útil para sensores com ruído, movimento, bateria, Filtros de Kalman,
 * dinâmina simples etc.
 */
fun interface SimContinuousModel {
    fun step(context: SimProcessContext, deltaTime: Double)
}