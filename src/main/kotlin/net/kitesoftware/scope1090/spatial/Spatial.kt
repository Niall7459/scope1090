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
    val originLat = Math.toRadians(origin.latitude)
    val destinationLat = Math.toRadians(destination.latitude)
    val diffLong = Math.toRadians(destination.longitude - origin.longitude)

    val bearing = atan2(sin(diffLong) * cos(destinationLat),
            cos(originLat) * sin(destinationLat) -
                    sin(originLat) * cos(destinationLat) * cos(diffLong))

    return if (bearing < 0) bearing + Math.PI * 2 else bearing
}

fun calculateDistanceBetweenPoints(origin: Coordinate, destination: Coordinate): Double {
    val latDistance = Math.toRadians(destination.latitude - origin.latitude)
    val lonDistance = Math.toRadians(destination.longitude - origin.longitude)
    val a = (sin(latDistance / 2) * sin(latDistance / 2)
            + (cos(Math.toRadians(origin.latitude)) * cos(Math.toRadians(destination.latitude))
            * sin(lonDistance / 2) * sin(lonDistance / 2)))
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return EARTH_RADIUS * c
}