package br.com.pedromagno.core

class SimClock {
    var now: SimTime = SimTime.ZERO
        private set

    fun advanceTo(time: SimTime) {
        require(time >= now) {
            "O relógio não pode voltar no tempo. Atual: $now, novo: $time"
        }

        now = time
    }

    fun reset() {
        now = SimTime.ZERO
    }
}