package net.kitesoftware.scope1090.radar

import net.kitesoftware.scope1090.SCOPE_FRAMES_PER_SECOND
import net.kitesoftware.scope1090.SCOPE_SWEEP_RPM

/**
 * Created by niall on 20/05/2020.
 */
class RadarSweep(private val radar: Radar) {
    var sweepRotation = 0.0
    var sweepPeriod = 0.0

    init {
        sweepPeriod = calculateSweepPeriod()
    }

    /**
     * Update the sweep rotation
     */
    fun updateSweepRotation() {
        if (SCOPE_SWEEP_RPM < 1) {
            return
        }

        if (sweepRotation >= Math.PI * 2) {
            sweepRotation -= (Math.PI * 2) //prevents jitter caused by = 0
        } else {
            sweepRotation += calculateRadiansPerFrame()
        }

        radar.checkInterrogateTracks()
    }

    /**
     * Calculate the increment of radians per frame
     */
    private fun calculateRadiansPerFrame(): Double {
        val rotationsPerSecond = SCOPE_SWEEP_RPM / 60.0
        val radiansPerSecond = (Math.PI * 2) * rotationsPerSecond

        return radiansPerSecond / SCOPE_FRAMES_PER_SECOND
    }

    /**
     * Calculate the sweep period
     */
    fun calculateSweepPeriod(): Double {
        return 1 / (SCOPE_SWEEP_RPM / 60.0)
    }

    /**
     * Get the current sweep rotation in degrees
     */
    fun sweepRotationInDegrees(): Double {
        return Math.toDegrees(sweepRotation)
    }
}