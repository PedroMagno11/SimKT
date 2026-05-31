package br.com.pedromagno.utils.coords

interface Coord<C> {
    fun distanceTo(other: C): Double
}