package br.com.pedromagno.core

import java.util.UUID

abstract class SimEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String
) {
    var createdAt: SimTime ? = null
        private set
    var destroyedAt: SimTime ? = null
        private set
    val isDestroyed: Boolean
        get() = destroyedAt != null

    open fun onCreate(env: Environment) {
        createdAt = env.now
    }

    open fun onDestroy(env: Environment) {
        destroyedAt = env.now
    }
}