package br.com.pedromagno.core

@JvmInline
value class SimTime(val value: Double): Comparable<SimTime> {
    init{
        require(value >= 0.0) { "O tempo da simulação não pode ser negativo." }
    }

    operator fun plus(other: SimTime): SimTime {
        return SimTime(this.value + other.value)
    }

    operator fun plus(delay: Double): SimTime {
        require(delay >= 0) { "O delay não pode ser negativo." }
        return SimTime(this.value + delay)
    }

    override fun compareTo(other: SimTime): Int {
        return this.value.compareTo(other.value)
    }

    override fun toString(): String {
        return "SimTime(value=$value)"
    }

    companion object{
        val ZERO = SimTime(0.0)
    }

}