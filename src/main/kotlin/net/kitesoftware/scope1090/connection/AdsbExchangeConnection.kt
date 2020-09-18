package net.kitesoftware.scope1090.connection

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import net.kitesoftware.scope1090.radar.Radar
import net.kitesoftware.scope1090.radar.track.RadarTrack
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.fixedRateTimer

/**
 * Created by niall on 06/08/2020.
 */
const val BASE_URL = "https://adsbexchange.com/api/aircraft/json/lat/{lat}/lon/{lon}/dist/{distNm}/"
const val UPDATE_PERIOD = 7_000L

class AdsbExchangeConnection(private val key: String) : Connection() {
    private val jsonFactory = JsonFactory()

    override fun connect(radar: Radar) {
        fixedRateTimer("adsbExchangeFetcher", false, 0, UPDATE_PERIOD) {

            //only request the distance we need to.
            val requestDist = mToNm(radar.screen.scope.calculateVisibleRange()
                    .coerceAtMost(250000.0)).toString()

            //build adsb exchange request url.
            val requestUrl = BASE_URL
                    .replace("{lat}", radar.origin.latitude.toString())
                    .replace("{lon}", radar.origin.longitude.toString())
                    .replace("{distNm}", requestDist)

            try {
                makeRequest(requestUrl, radar)
            } catch (exception: Exception) {
                //print exception but continue connection attempts
                exception.printStackTrace()
            }
        }
    }

    private fun makeRequest(requestUrl: String, radar: Radar) {
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("user-agent", "scope1090-adsbx")
        connection.setRequestProperty("api-auth", key)

        val inputStream = connection.inputStream
        val parser = jsonFactory.createParser(inputStream)

        while (parser.nextToken() != null) {
            if (parser.currentName == "ac") {
                parseAircraft(parser, radar)
            }
        }

        connection.disconnect()
    }

    private fun parseAircraft(parser: JsonParser, radar: Radar) {
        lateinit var track: RadarTrack

        while (parser.nextToken() != null) {
            when (parser.currentName) {
                "icao" -> {
                    track = radar.getOrCreateRadarTrack(parser.nextTextValue().toInt(16))

                    //not proud of this:
                    //due to rate limits we cant query faster than we are.
                    //this means the track disappear after one sweep.
                    //sweep has to either be running very slow to prevent this.
                    //or we fake the time it was heard.
                    track.lastHeard = System.currentTimeMillis() + UPDATE_PERIOD
                }
                "call" -> {
                    track.identity = parser.nextTextValue()
                }
                "alt" -> {
                    track.altitude = parser.nextIntValue(0)
                }
                "sqk" -> {
                    track.modeA = parser.nextTextValue()
                }
                "lat" -> {
                    val lat: String = parser.nextTextValue()
                    if (lat.isNotEmpty()) {
                        track.latitude = lat.toDouble()
                    }
                }
                "lon" -> {
                    val lon: String = parser.nextTextValue()
                    if (lon.isNotEmpty()) {
                        track.longitude = lon.toDouble()
                        track.updatePos(radar)
                    }
                }
                "gnd" -> {
                    val gnd: String = parser.nextTextValue()
                    if (gnd.isNotEmpty()) {
                        track.onGround = gnd == "1"
                    }
                }
                else -> {
                    parser.nextToken()
                }
            }
        }
    }

    override fun disconnect(radar: Radar) {

    }
}

fun mToNm(m: Double): Double {
    return m * 0.000539957
}