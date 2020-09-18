package net.kitesoftware.scope1090.radar.track

import net.kitesoftware.scope1090.SCOPE_SWEEP_RPM
import net.kitesoftware.scope1090.radar.Radar
import net.kitesoftware.scope1090.spatial.Coordinate
import net.kitesoftware.scope1090.spatial.calculateBearingBetweenPoints
import net.kitesoftware.scope1090.spatial.calculateDistanceBetweenPoints
import java.awt.geom.Point2D
import java.awt.image.BufferedImage

/**
 * Created by niall on 20/05/2020.
 */
data class RadarTrack(val address: Int) {
    var plottable = false

    var latitude = 0.0
    var longitude = 0.0

    //bearing and distance updated when a new position is received
    var realBearing = 0.0
    var realDist = 0.0

    //interrogation bearing set to real bearing when interrogated
    var interrogBearing = 0.0
    var interrogDist = 0.0
    var interrogTime = 0L

    //cached point to use for drawing, instead of re-calculating
    var screenPoint: Point2D? = null

    //also cache the callout instead of rendering text each frame
    var screenCallout: BufferedImage? = null

    //cached distance in the scope context, after being scaled.
    var screenDist: Int? = null

    //track id details
    var identity: String? = null
        set(value) {
            field = checkRepaintRequired(identity, value)
        }

    //track squawk code
    var modeA: String? = null
        set(value) {
            field = checkRepaintRequired(modeA, value)
        }

    var onGround = false
        set(value) {
            field = checkRepaintRequired(onGround, value)
        }

    var altitude: Int? = null

    var spiTime = 0L

    var lastHeard = 0L

    /**
     * Interrogate this track
     */
    fun interrogate() {
        interrogBearing = realBearing
        interrogDist = realDist
        interrogTime = System.currentTimeMillis()

        //invalidate point to be re-calculated
        screenPoint = null
        screenDist = null
    }

    fun updatePos(radar: Radar) {
        val coordinate = Coordinate(latitude, longitude)

        realBearing = calculateBearingBetweenPoints(radar.origin, coordinate)
        realDist = calculateDistanceBetweenPoints(radar.origin, coordinate)

        plottable = true

        if (SCOPE_SWEEP_RPM < 1) {
            interrogate()
        }
    }

    /**
     * Is the special positional indicator (spi) is active
     * This returns true for 5 seconds after being activated
     */
    fun isSpiActivated(): Boolean {
        return System.currentTimeMillis() - spiTime < 5000
    }

    /**
     * Milliseconds elapsed since it was interrogated
     */
    fun elapsedSinceInterrogation(): Long {
        return System.currentTimeMillis() - interrogTime
    }

    /**
     * Check if two values are different then invalidate the callout if required
     */
    private fun <T> checkRepaintRequired(oldVal: T, newVal: T): T {
        if (oldVal != newVal) {
            //invalidate the screen callout
            screenCallout = null
        }

        return newVal
    }
}