package br.com.pedromagno.communication.topic

class SimTopic<T> {
    /**
     * Map<String (nome do evento), Valor (Uma lista de funções sem retorno).
     */
    private val actuators = mutableMapOf<String, MutableList<(Any?) -> Unit>>()

    fun on(eventName: String, action: (Any?) -> Unit){
        actuators
            .getOrPut(eventName) { mutableListOf() }
            .add(action)
    }

    fun emit(eventName: String, payload: Any? = null) {
        actuators[eventName]?.forEach {
            actuator -> actuator(payload)
        }
    }
}