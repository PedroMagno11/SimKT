package br.com.pedromagno.event

data class SimScheduledEvent(
    val event: SimEvent<*>,
    val sequence: Long,
): Comparable<SimScheduledEvent> {

    override fun compareTo(other: SimScheduledEvent): Int {
        val timeComparison = this.event.time.compareTo(other.event.time)

        if (timeComparison != 0){
            return timeComparison
        }

        return this.sequence.compareTo(other.sequence)
    }
}