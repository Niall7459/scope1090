package net.kitesoftware.scope1090.spatial

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val EARTH_RADIUS = 6371000

/**
 * Created by niall on 21/05/2020.
 */
fun calculateBearingBetweenPoints(origin: Coordinate, destination: Coordinate): Double {
    val diffLong = destination.lam - origin.lam

    val bearing = atan2(
            sin(diffLong) * cos(destination.phi),
            cos(origin.phi) * sin(destination.phi) - sin(origin.phi) * cos(destination.phi) * cos(diffLong))

    return if (bearing < 0) bearing + Math.PI * 2 else bearing
}

fun calculateDistanceBetweenPoints(origin: Coordinate, destination: Coordinate): Double {
    val diffLat = destination.phi - origin.phi
    val diffLon = destination.lam - origin.lam

    val a = (sin(diffLat / 2) * sin(diffLat / 2)
            + (cos(origin.phi) * cos(destination.phi)
            * sin(diffLon / 2) * sin(diffLon / 2)))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return EARTH_RADIUS * c
}