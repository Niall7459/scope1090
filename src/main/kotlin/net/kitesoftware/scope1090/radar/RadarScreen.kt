package net.kitesoftware.scope1090.radar

import net.kitesoftware.scope1090.*
import net.kitesoftware.scope1090.radar.component.ButtonPosition
import net.kitesoftware.scope1090.radar.component.RadarButton
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

/**
 * Created by niall on 20/05/2020.
 */
class RadarScreen(val radar: Radar) : JPanel(), ActionListener {
    var scope = RadarScope(this)
    var prevSweepBounds: Rectangle? = null

    private val leftHandButtons = mutableListOf<RadarButton>()
    private val rightHandButtons = mutableListOf<RadarButton>()

    init {
        val mouseListener = RadarMouseListener(this)
        addMouseWheelListener(mouseListener)
        cursor = createCursorIcon()

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(componentEvent: ComponentEvent) {
                scope.updateScope()
                repaint()
            }
        })

        leftHandButtons.add(RadarButton("CALL", true, ButtonPosition.LEFT))
        leftHandButtons.add(RadarButton("SQK", true, ButtonPosition.LEFT))
        leftHandButtons.add(RadarButton("B3", false, ButtonPosition.LEFT))
        leftHandButtons.add(RadarButton("B4", false, ButtonPosition.LEFT))
        leftHandButtons.add(RadarButton("B5", false, ButtonPosition.LEFT))
        leftHandButtons.add(RadarButton("B6", false, ButtonPosition.LEFT))

        rightHandButtons.add(RadarButton("A1", false, ButtonPosition.RIGHT))
        rightHandButtons.add(RadarButton("A2", false, ButtonPosition.RIGHT))
        rightHandButtons.add(RadarButton("A3", false, ButtonPosition.RIGHT))
        rightHandButtons.add(RadarButton("A4", false, ButtonPosition.RIGHT))
        rightHandButtons.add(RadarButton("A5", false, ButtonPosition.RIGHT))
        rightHandButtons.add(RadarButton("A6", false, ButtonPosition.RIGHT))

        val timer = Timer(1000 / SCOPE_FRAMES_PER_SECOND, this)
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

        if (SCOPE_BUTTONS_ENABLED) {
            paintSideButtons(graphics)
        }

        scope.paintScope(graphics)
        paintScopeText(graphics)
    }

    /**
     * Create the radar scope cursor icon
     */
    private fun createCursorIcon(): Cursor {
        //we have to use the default configuration, because this JPanel's GraphicsConfiguration is not initialized yet.
        val graphicsConfiguration = GraphicsEnvironment
                .getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration

        //always use 32 size to ensure windows compatibility.
        val cursorImage = graphicsConfiguration.createCompatibleImage(32, 32, BufferedImage.TYPE_INT_ARGB)

        val graphics = cursorImage.createGraphics()
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
        graphics.color = SCOPE_PRIMARY_COLOR
        graphics.font = FONT_TRACK
        graphics.drawString("ROTATION: " + round(scope.rotation), 10, 20)
        graphics.drawString("RANGE: " + scope.calculateVisibleRange() / 1000 + " KM", 10, 35)
        graphics.drawString("TRACKS: " + radar.activeTracks().size, 10, 50)

        if (SCOPE_SWEEP_RPM > 0) {
            graphics.drawString("RPM: $SCOPE_SWEEP_RPM", 10, 65)
        }
    }

    private fun paintSideButtons(graphics: Graphics2D) {
        val buttonSpacing = height / 7

        var y = buttonSpacing
        for (button in leftHandButtons) {
            button.draw(graphics, 20, y - 25)
            y += buttonSpacing
        }

        y = buttonSpacing
        for (button in rightHandButtons) {
            button.draw(graphics, width - 50 - 20, y - 25)
            y += buttonSpacing
        }
    }

    /**
     * Action handler
     */
    override fun actionPerformed(e: ActionEvent?) {
        radar.sweep.updateSweepRotation()

        //if scale changed update whole screen
        if (scope.scaleChanged || scope.scopeChanged) {
            repaint()
            return
        }

        //repaint track callouts
        for (track in radar.activeTracks()) {
            this.repaint(scope.calculateTrackBounds(track) ?: continue)
        }

        if (SCOPE_SWEEP_RPM > 0) {
            repaintSweepBounds()
        }

        //note: all these repaint(...) calls will be
        //reduced into one with their boundaries merged.
    }

    private fun repaintSweepBounds() {
        val sweepBounds = scope.calculateSweepBounds()
        this.repaint(sweepBounds)

        //update previous bounds, so we dont leave behind a mess
        if (prevSweepBounds != null) {
            this.repaint(prevSweepBounds)
        }

        this.prevSweepBounds = sweepBounds
    }

    override fun createImage(width: Int, height: Int): BufferedImage {
        return graphicsConfiguration.createCompatibleImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE)
    }

    fun drawImage(graphics: Graphics2D, image: Image?, x: Int, y: Int) {
        graphics.drawImage(image, x, y, null)
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

fun Graphics2D.applyHDRenderingHints() {
    if (!SCOPE_HD) {
        return
    }

    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

    //text aliasing off - causes issues with the small fonts
    setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
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