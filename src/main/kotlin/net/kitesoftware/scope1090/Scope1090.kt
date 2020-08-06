package net.kitesoftware.scope1090

import net.kitesoftware.scope1090.connection.BaseStationConnection
import net.kitesoftware.scope1090.radar.Radar
import net.kitesoftware.scope1090.radar.RadarScreen
import net.kitesoftware.scope1090.spatial.Coordinate
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

class Scope1090 : JFrame() {
    val radar = Radar()
    private val radarScreen = RadarScreen(radar)

    init {
        title = "Scope1090"
        size = Dimension(600, 600)
        layout = BorderLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        add(radarScreen, BorderLayout.CENTER)
    }
}

fun main(args: Array<String>) {
    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())

    SwingUtilities.invokeLater {
        val scope1090 = Scope1090()

        parseOrigin(args, scope1090.radar)
        parseConnections(args, scope1090.radar)

        scope1090.isVisible = true;
    }
}

fun parseOrigin(args: Array<String>, radar: Radar) {
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
        if (!arg.startsWith("--server=")) {
            continue
        }

        val parts = arg.split("=")[1].split(":")
        val hostname = parts[0]
        val port = parts[1].toInt()

        val conn = BaseStationConnection(hostname, port)
        radar.addConnection(conn)
    }
}
