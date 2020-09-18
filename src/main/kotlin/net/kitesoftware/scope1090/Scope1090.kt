package net.kitesoftware.scope1090

import net.kitesoftware.scope1090.connection.AdsbExchangeConnection
import net.kitesoftware.scope1090.connection.BaseStationConnection
import net.kitesoftware.scope1090.radar.Radar
import net.kitesoftware.scope1090.spatial.Coordinate
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.nio.file.StandardCopyOption
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

class Scope1090 : JFrame() {
    val radar = Radar()

    init {
        title = "Scope1090"
        size = Dimension(600, 600)
        minimumSize = Dimension(400, 400)
        layout = BorderLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        add(radar.screen, BorderLayout.CENTER)

        addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent?) {
                //set default range to 250 km.
                radar.screen.scope.setVisibleRange(250_000.0)
            }
        })

        if (SCOPE_FULLSCREEN) {
            isUndecorated = true
            graphicsConfiguration.device.fullScreenWindow = this
        }
    }
}

/**
 * Program entry point
 */
fun main(args: Array<String>) {
    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())

    parseSettings(args)

    SwingUtilities.invokeLater {
        val scope1090 = Scope1090()

        parseRequiredOrigin(args, scope1090.radar)
        parseConnections(args, scope1090.radar)
        scope1090.isVisible = true
    }
}

fun parseRequiredOrigin(args: Array<String>, radar: Radar) {
    var lat = 0.0
    var lon = 0.0

    for (arg in args) {
        if (arg.startsWith("--lat=")) {
            lat = arg.split("=")[1].toDouble()
        }

        if (arg.startsWith("--lon=")) {
            lon = arg.split("=")[1].toDouble()
        }
    }

    radar.origin = Coordinate(lat, lon)
}

fun parseConnections(args: Array<String>, radar: Radar) {
    for (arg in args) {
        when {
            arg.startsWith("--sbs=") -> {
                parseSbsInput(arg, radar)
            }
            arg.startsWith("--adsbx=") -> {
                parseAdsbExchangeInput(arg, radar)
            }
        }
    }
}

/**
 * Parse sbs connection
 */
private fun parseSbsInput(arg: String, radar: Radar) {
    val connectionStringArgs = arg.split("=")[1].split(":")
    val connection = BaseStationConnection(connectionStringArgs[0], connectionStringArgs[1].toInt())

    radar.addConnection(connection)
}

/**
 * Parse adsb exchange connection
 */
private fun parseAdsbExchangeInput(arg: String, radar: Radar) {
    val adsbExchangeAccessKey = arg.split("=")[1]
    val connection = AdsbExchangeConnection(adsbExchangeAccessKey)

    radar.addConnection(connection)
}
