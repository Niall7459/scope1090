package net.kitesoftware.scope1090.radar

import kotlin.concurrent.fixedRateTimer

const val FRAMES_PER_SECOND = 30
const val ROTATIONS_PER_MINUTE = 24

/**
 * Created by niall on 20/05/2020.
 */
class RadarSweep(private val radar: Radar) {
    var sweepRotation = 0.0
    var sweepPeriod = 0.0

    init {
        val timerPeriod = 1000L / FRAMES_PER_SECOND

        fixedRateTimer("sweepUpdater", false, 0, timerPeriod) {
            updateSweepRotation()
            radar.checkInterrogateTracks()
        }

        sweepPeriod = calculateSweepPeriod()
    }

    /**
     * Update the sweep rotation
     */
    private fun updateSweepRotation() {
        if (sweepRotation >= Math.PI * 2) {
            sweepRotation -= (Math.PI * 2) //prevents jitter caused by = 0
        } else {
            sweepRotation += calculateRadiansPerFrame()
        }
    }

    /**
     * Calculate the increment of radians per frame
     */
    private fun calculateRadiansPerFrame(): Double {
        val rotationsPerSecond = ROTATIONS_PER_MINUTE / 60.0
        val radiansPerSecond = (Math.PI * 2) * rotationsPerSecond

        return radiansPerSecond / FRAMES_PER_SECOND
    }

    /**
     * Calculate the sweep period
     */
    fun calculateSweepPeriod(): Double {
        return 1 / (ROTATIONS_PER_MINUTE / 60.0)
    }

    /**
     * Get the current sweep rotation in degrees
     */
    fun sweepRotationInDegrees(): Double {
        return Math.toDegrees(sweepRotation)
    }
}