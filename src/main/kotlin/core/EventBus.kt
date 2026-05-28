package br.com.pedromagno.core


class EventBus {
    /**
     * Map<String (nome do evento), Valor (Uma lista de funções sem retorno).
     */
    private val listeners = mutableMapOf<String, MutableList<(Any?) -> Unit>>()

    fun on(eventName: String, action: (Any?) -> Unit){
        listeners
            .getOrPut(eventName) { mutableListOf() }
            .add(action)
    }

    fun emit(eventName: String, payload: Any? = null) {
        listeners[eventName]?.forEach {
            listener -> listener(payload)
        }
    }
}