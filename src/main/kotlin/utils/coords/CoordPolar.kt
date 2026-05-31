package br.com.pedromagno.utils.coords

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

data class CoordPolar(
    var bearing: Double,
    var distance: Double,
): Coord<CoordPolar> {
    override fun distanceTo(other: CoordPolar): Double {
// Converte os ângulos para radianos caso estejam em graus
        val t1 = bearing * PI / 180.0
        val t2 = other.bearing * PI / 180.0

        // Aplicação direta da Lei dos Cossenos
        val r1Sq = distance * distance
        val r2Sq = other.distance * other.distance
        val cosDiff = cos(t2 - t1)

        return sqrt(r1Sq + r2Sq - 2 * distance * other.distance * cosDiff)
    }
}