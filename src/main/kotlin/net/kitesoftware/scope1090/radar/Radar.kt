package net.kitesoftware.scope1090.radar

import net.kitesoftware.scope1090.connection.Connection
import net.kitesoftware.scope1090.radar.track.RadarTrack
import net.kitesoftware.scope1090.spatial.Coordinate
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.fixedRateTimer

/**
 * Created by niall on 20/05/2020.
 */
class Radar {
    private val tracks = ConcurrentHashMap<Int, RadarTrack>()
    private val connections = mutableListOf<Connection>()
    val sweep = RadarSweep(this)
    val screen = RadarScreen(this)
    lateinit var origin: Coordinate

    init {
        //run the track expiry function every second.
        fixedRateTimer("trackExpiry", false, 1000, 1000) {
            expireInactiveTracks()
        }
    }

    /**
     * Expire any tracks not seen for over 60 seconds.
     */
    private fun expireInactiveTracks() {
        for (track in tracks.values) {
            //keep track's saved for at least 60 seconds
            if (System.currentTimeMillis() - track.lastHeard > 60_000) {
                tracks.remove(track.address)
            }
        }
    }

    /**
     * Get (or create, if not existing) a radar track.
     * @param address track address
     */
    fun getOrCreateRadarTrack(address: Int): RadarTrack {
        var radarTrack = tracks[address]

        if (radarTrack == null) {
            radarTrack = RadarTrack(address)
            tracks[address] = radarTrack
        }

        return radarTrack
    }

    /**
     * Check and 'interrogate' radar tracks
     * This creates the effect of a traditional radar interrogation.
     */
    fun checkInterrogateTracks() {
        for (track in activeTracks()) {
            //if no location is known, track can't be interrogated.
            if (!track.plottable) {
                continue
            }

            if (System.currentTimeMillis() - track.lastHeard > sweep.calculateSweepPeriod() * 2000) {
                //nothing received since last interrogation, don't 'interrogate'
                continue
            }

            //is the track near the sweep line?
            if (track.realBearing in sweep.sweepRotation - 0.05..sweep.sweepRotation + 0.05) {
                //'interrogate' the track
                track.interrogate()
            }
        }
    }

    /**
     * Return all active tracks
     */
    fun activeTracks(): Collection<RadarTrack> {
        return tracks.values
    }

    /**
     * Add and start a new connection.
     */
    fun addConnection(connection: Connection) {
        connections.add(connection)
        connection.connect(this)
    }
}