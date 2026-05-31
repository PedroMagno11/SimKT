package br.com.pedromagno.utils.coords

import kotlin.math.sqrt
data class CoordXYZ(
    var x: Double,
    var y: Double,
    var z: Double,
): Coord<CoordXYZ> {
    override fun distanceTo(other: CoordXYZ): Double {
        val dx = other.x - x
        val dy = other.y - y
        val dz = other.z - z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

}