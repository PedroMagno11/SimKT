package br.com.pedromagno.monitor

import br.com.pedromagno.core.SimTime

class SimMonitor {
    private val records: MutableList<String> = mutableListOf()

    fun record(time: SimTime, message: String) {
        records.add("[$time]: $message")
    }

    fun all(): List<String> = records.toList()

    fun printAll() {
        records.forEach { println(it) }
    }

    fun clear() {
        records.clear()
    }
}
