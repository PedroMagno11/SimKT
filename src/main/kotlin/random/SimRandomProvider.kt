package br.com.pedromagno.random

import kotlin.random.Random

class SimRandomProvider(seed: Long? = null) {

    private val random: Random = seed ?.let { Random(it) } ?: Random.Default

    fun nextInt(): Int{
        return random.nextInt()
    }

    fun nextInt(from: Int, until: Int): Int {
        return random.nextInt(from, until)
    }

    fun nextLong(): Long{
        return random.nextLong()
    }

    fun nextLong(from: Long, until: Long): Long {
        return random.nextLong(from, until)
    }

    fun nextDouble(): Double {
        return random.nextDouble()
    }

    fun nextDouble(from: Double, until: Double): Double {
        return random.nextDouble(from, until)
    }

    fun nextBoolean(): Boolean {
        return random.nextBoolean()
    }

}

