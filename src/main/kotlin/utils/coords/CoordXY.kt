package br.com.pedromagno.utils.coords

import kotlin.math.sqrt

data class CoordXY(
    var x: Double,
    var y: Double,
): Coord<CoordXY> {
    override fun distanceTo(other: CoordXY): Double {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }

}