package br.com.pedromagno.process

fun interface ISimProcess {
    suspend fun run(context: SimProcessContext)
}