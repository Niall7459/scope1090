package net.kitesoftware.scope1090.connection

import net.kitesoftware.scope1090.radar.Radar
import net.kitesoftware.scope1090.spatial.Coordinate
import net.kitesoftware.scope1090.spatial.calculateBearingBetweenPoints
import net.kitesoftware.scope1090.spatial.calculateDistanceBetweenPoints
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

const val FIELD_ADDR = 4
const val FIELD_CALL = 10
const val FIELD_ALT = 11
const val FIELD_LAT = 14
const val FIELD_LON = 15
const val FIELD_MODEA = 17
const val FIELD_SPI = 20

/**
 * Created by niall on 21/05/2020.
 */
class BaseStationConnection(private val host: String, private val port: Int) : Connection() {
    lateinit var radar: Radar
    lateinit var reader: InputStreamReader
    private lateinit var socket: Socket

    override fun connect(radar: Radar) {
        this.radar = radar

        val thread = Thread() {
            this.socket = Socket(host, port)
            this.reader = InputStreamReader(socket.getInputStream())

            BufferedReader(reader).use { reader ->
                reader.lineSequence().forEach {
                    handleBaseStationData(it)
                }
            }
        }

        thread.start();
    }

    override fun disconnect(radar: Radar) {
        this.socket.close()
    }

    private fun handleBaseStationData(data: String) {
        val dataFields = data.split(",")

        //drop message if no address
        if (!isFieldValid(dataFields, FIELD_ADDR)) {
            return
        }

        val radarTrack = radar.getOrCreateRadarTrack(Integer.parseInt(dataFields[FIELD_ADDR], 16))
        radarTrack.lastHeard = System.currentTimeMillis()

        if (isFieldValid(dataFields, FIELD_CALL)) {
            val callSign = dataFields[FIELD_CALL]
            if (!radarTrack.identity.equals(callSign)) {
                radarTrack.identity = callSign

                //invalidate callout so it's redrawn
                radarTrack.screenCallout = null
            }
        }

        if (isFieldValid(dataFields, FIELD_MODEA)) {
            val squawkCode = dataFields[FIELD_MODEA]
            if (!radarTrack.modeA.equals(squawkCode)) {
                radarTrack.modeA = squawkCode
                radarTrack.screenCallout = null
            }
        }

        if (isFieldValid(dataFields, FIELD_SPI)) {
            if (dataFields[FIELD_SPI] == "-1") {
                radarTrack.spiTime = System.currentTimeMillis()
            }
        }

        if (isFieldValid(dataFields, FIELD_LAT) && isFieldValid(dataFields, FIELD_LON)) {
            val latitude = dataFields[FIELD_LAT].toDouble()
            val longitude = dataFields[FIELD_LON].toDouble()
            val coordinate = Coordinate(latitude, longitude)

            //calculate bearing
            radarTrack.realBearing = calculateBearingBetweenPoints(radar.origin, coordinate)
            radarTrack.realDist = calculateDistanceBetweenPoints(radar.origin, coordinate)
            radarTrack.plottable = true
        }

        if (isFieldValid(dataFields, FIELD_ALT)) {
            val altitude = dataFields[FIELD_ALT].toInt()
            radarTrack.altitude = altitude
        }
    }

    private fun isFieldValid(dataFields: List<String>, fieldId: Int): Boolean {
        return dataFields.size > fieldId && !dataFields[fieldId].isEmpty()
    }
}