package net.kitesoftware.scope1090.radar

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

const val RANGE_MARKER_INTERVAL_KM = 50

/**
 * Created by niall on 20/05/2020.
 */
class RadarScreen(val radar: Radar) : JPanel(), ActionListener {
    var scope = RadarScope(this)
    val gfxConfig: GraphicsConfiguration? =
            GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
    var prevSweepBounds: Rectangle? = null

    init {
        val mouseListener = RadarMouseListener(this)
        addMouseWheelListener(mouseListener)
        cursor = createCursorIcon()

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(componentEvent: ComponentEvent) {
                scope.updateScope()

                //repaint whole screen
                repaint()
            }
        })

        val timer = Timer(25, this)
        timer.isRepeats = true
        timer.start()
    }

    /**
     * Paint the radar screen
     * @param _graphics Graphics
     */
    override fun paintComponent(_graphics: Graphics) {
        val graphics: Graphics2D = _graphics as Graphics2D

        //fill black background
        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, width, height)

        scope.paintScope(graphics)
        paintScopeText(graphics)
    }

    /**
     * Create the radar scope cursor icon
     */
    private fun createCursorIcon(): Cursor {
        //always use 32 size to ensure windows compatibility.
        val cursorImage = gfxConfig?.createCompatibleImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        val graphics = cursorImage?.createGraphics()!!
        graphics.color = Color.LIGHT_GRAY

        graphics.drawLine(7, 0, 7, 14)
        graphics.drawLine(0, 7, 14, 7)

        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, Point(7, 7), "Crosshair")
    }

    /**
     * Paint radar scope text.
     * @param graphics Graphics2D
     */
    private fun paintScopeText(graphics: Graphics2D) {
        graphics.color = Color.GREEN
        graphics.font = FONT_TRACK
        graphics.drawString("ROTATION: " + round(scope.rotation), 10, 20)
        graphics.drawString("RANGE: " + scope.calculateVisibleRange() / 1000 + " KM", 10, 40)
        graphics.drawString("TRACKS: " + radar.activeTracks().size, 10, 60)
        graphics.drawString("RPM: $ROTATIONS_PER_MINUTE", 10, 80)
    }

    /**
     * Action handler
     */
    override fun actionPerformed(e: ActionEvent?) {
        //if scale changed update whole screen
        if (scope.scaleChanged || scope.scopeChanged) {
            repaint()
            return
        }

        //calculate update region for sweep line
        val sweepBounds = scope.calculateSweepBounds()
        this.repaint(sweepBounds)

        //update previous bounds
        if (prevSweepBounds != null) {
            this.repaint(prevSweepBounds)
        }

        this.prevSweepBounds = sweepBounds

        //repaint track callouts
        for (track in radar.activeTracks()) {
            val point = track.screenPoint
            val callout = track.screenCallout

            if (point != null && callout != null) {
                this.repaint(point.x.toInt() - 5, point.y.toInt() - 5, callout.width + 5, callout.height + 5)
            }
        }
    }
}

/**
 * Calculate point from origin point, bearing and length
 * @param oX origin X
 * @param oY origin Y
 * @param angle bearing to project point
 * @param length length to project point
 */
fun calculatePoint(oX: Int, oY: Int, angle: Double, length: Int): Point2D {
    val tX = oX + length * sin(angle)
    val tY = oY + length * cos(angle)

    return Point2D.Double(round(tX), round(tY))
}

/**
 * Draw line with bearing and length
 *
 * @param oX origin X
 * @param oY origin Y
 * @param angle bearing of line
 * @param length length of the line
 */
fun Graphics2D.drawLine(oX: Int, oY: Int, angle: Double, length: Int) {
    val tX = oX + length * sin(angle)
    val tY = oY + length * cos(angle)

    this.drawLine(oX, oY, tX.toInt(), tY.toInt())
}

/**
 * Draw a centered string
 *
 * @param text text to draw
 * @param x x position
 * @param y y position
 */
fun Graphics2D.drawStringCentered(text: String, x: Int, y: Int) {
    val sX = x - (this.fontMetrics.stringWidth(text) / 2)
    val sY = y + (this.fontMetrics.ascent / 2)
    this.drawString(text, sX, sY)
}