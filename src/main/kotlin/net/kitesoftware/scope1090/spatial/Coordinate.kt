package net.kitesoftware.scope1090.spatial

/**
 * Created by niall on 21/05/2020.
 */
data class Coordinate(val latitude: Double, val longitude: Double) {
    //cached values
    val phi: Double = Math.toRadians(latitude)
    val lam: Double = Math.toRadians(longitude)
}