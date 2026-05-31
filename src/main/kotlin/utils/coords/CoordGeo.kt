package br.com.pedromagno.utils.coords

import kotlin.math.*

data class CoordGeo(
    var lat: Double,
    var lng: Double,
) : Coord<CoordGeo> {

    override fun distanceTo(other: CoordGeo): Double {
        val raioTerraEmMetros = 6_371_000

        val dLat = Math.toRadians(other.lat - lat)
        val dLon = Math.toRadians(other.lng - lng)

        val lat1Rad = Math.toRadians(lat)
        val lat2Rad = Math.toRadians(other.lat)

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))

        return raioTerraEmMetros * c
    }
}