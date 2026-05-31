package br.com.pedromagno.process

import br.com.pedromagno.communication.channel.SimChannel
import br.com.pedromagno.container.SimContainer
import br.com.pedromagno.core.Environment
import br.com.pedromagno.core.SimTime
import br.com.pedromagno.event.SimEvent
import br.com.pedromagno.resource.SimResource
import br.com.pedromagno.resource.SimResourceRequest
import br.com.pedromagno.store.SimQueue
import kotlin.coroutines.suspendCoroutine

class SimProcessContext (
    val env: Environment,
){
    val now: SimTime
        get() = env.now

    suspend fun hold(delay: Double){
        val event = env.timeout(delay)
        event.awaitSuspend()
    }

    suspend fun hold(delay: Long){
        hold(delay.toDouble())
    }

    suspend fun <T> await(event: SimEvent<T>): T? {
        return event.awaitSuspend()
    }


    suspend fun request(resource: SimResource): SimResourceRequest {
        return await(resource.request()) ?: error("A requisição do recurso ${resource.name} foi cancelada ou não retornou token.")
    }

    fun release(resource: SimResource, request: SimResourceRequest){
        resource.release(request)
    }

    suspend fun <T> send(channel: SimChannel<T>, value: T){
        await(channel.send(value))
    }

    suspend fun <T> receive(channel: SimChannel<T>) : T {
        return await(channel.receive()) ?: error("Recebimento cancelado ou sem valor.")
    }

    suspend fun <T> put(store: SimQueue<T>, value: T){
        await(store.put(value))
    }

    suspend fun put(container: SimContainer, amout: Double){
        await(container.put(amout))
    }

    suspend fun <T> get(store: SimQueue<T>): T {
        return await(store.get()) ?: error("Store cancelado ou sem valor.")
    }

    suspend fun get(container: SimContainer, amout: Double) {
        await(container.get(amout))
    }
    private suspend fun <T> SimEvent<T>.awaitSuspend(): T? {
        return suspendCoroutine { continuation ->
            this.await(continuation)
        }
    }
}