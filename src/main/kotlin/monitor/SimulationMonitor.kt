package br.com.pedromagno.monitor

import br.com.pedromagno.core.SimTime

class SimulationMonitor {
    private val records: MutableList<String> = mutableListOf()

    fun record(time: SimTime, message: String){
        records.add("[$time]: $message")
    }

    fun all(): List<String>{
        return records.toList()
    }

    fun printAll(){
        records.forEach { record -> println(record) }
    }

    fun clear(){
        records.clear()
    }

}