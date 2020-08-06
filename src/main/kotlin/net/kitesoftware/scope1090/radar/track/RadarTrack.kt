package net.kitesoftware.scope1090.radar.track

import java.awt.geom.Point2D
import java.awt.image.BufferedImage

/**
 * Created by niall on 20/05/2020.
 */
data class RadarTrack(val address: Int) {
    var plottable = false

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

    //used to prevent appearing/reappearing when scaling the scope
    var screenDist: Int? = null

    //track id details
    var identity: String? = null
    var modeA: String? = null
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

    /**
     * Is the special positional indicator (SPI) / IDENT button pressed in the cockpit?
     * This remains active for around 5 seconds after being pressed.
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
}