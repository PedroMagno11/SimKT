package br.com.pedromagno.process

fun interface SimProcess {
    suspend fun run(context: SimProcessContext)
}
