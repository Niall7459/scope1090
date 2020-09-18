package net.kitesoftware.scope1090.connection

import net.kitesoftware.scope1090.radar.Radar
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

const val FIELD_ADDR = 4
const val FIELD_CALL = 10
const val FIELD_ALT = 11
const val FIELD_LAT = 14
const val FIELD_LON = 15
const val FIELD_MODEA = 17
const val FIELD_FLAG_SPI = 20
const val FIELD_FLAG_GROUND = 21

/**
 * Created by niall on 21/05/2020.
 */
class BaseStationConnection(private val host: String, private val port: Int) : Connection() {
    lateinit var radar: Radar
    lateinit var reader: InputStreamReader
    private lateinit var socket: Socket

    override fun connect(radar: Radar) {
        this.radar = radar

        val thread = Thread {
            this.socket = Socket(host, port)
            this.reader = InputStreamReader(socket.getInputStream())

            BufferedReader(reader).use { reader ->
                reader.lineSequence().forEach {
                    handleBaseStationData(it)
                }
            }
        }

        thread.start()
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
            radarTrack.identity = dataFields[FIELD_CALL]
        }

        if (isFieldValid(dataFields, FIELD_MODEA)) {
            radarTrack.modeA = dataFields[FIELD_MODEA]
        }

        if (isFieldValid(dataFields, FIELD_LAT) && isFieldValid(dataFields, FIELD_LON)) {
            radarTrack.latitude = dataFields[FIELD_LAT].toDouble()
            radarTrack.longitude = dataFields[FIELD_LON].toDouble()

            radarTrack.updatePos(radar)
        }

        if (isFieldValid(dataFields, FIELD_ALT)) {
            radarTrack.altitude = dataFields[FIELD_ALT].toInt()
        }

        radarTrack.onGround = isFlagActive(dataFields, FIELD_FLAG_GROUND)

        if (isFlagActive(dataFields, FIELD_FLAG_SPI)) {
            radarTrack.spiTime = System.currentTimeMillis()
        }
    }

    private fun isFieldValid(dataFields: List<String>, fieldId: Int): Boolean {
        return dataFields.size > fieldId && !dataFields[fieldId].isEmpty()
    }

    private fun isFlagActive(inputData: List<String>, index: Int): Boolean {
        return if (!isFieldValid(inputData, index)) {
            false
        } else inputData[index] == "-1"
    }
}