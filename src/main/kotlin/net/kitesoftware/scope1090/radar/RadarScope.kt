package net.kitesoftware.scope1090.radar

import net.kitesoftware.scope1090.*
import net.kitesoftware.scope1090.radar.track.RadarTrack
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.math.round

val FONT = Font("Helvetica", Font.PLAIN, 10)
val FONT_TRACK = Font("Helvetica", Font.PLAIN, 10)

val STROKE_DEFAULT = BasicStroke(1f)
val STROKE_DASHED = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, floatArrayOf(9f), 0f)

/**
 * Created by niall on 22/05/2020.
 */
class RadarScope(private val screen: RadarScreen) {
    private var scopeImage: BufferedImage? = null
    private var rangeImage: BufferedImage? = null
    private var padding = 30
    var scaleChanged = false
    var scopeChanged = false
    var rotation = 0.0
    var diameter = 0
    var center = 0
    var scale = 1.0
    var size = 0
    var cursor = 0.0

    private var originX = 0
    private var originY = 0

    /**
     * Update dimension values
     */
    private fun updateDimensions() {
        size = calculateSize()
        diameter = calculateDiameter()
        center = calculateCenter()

        originX = screen.width / 2 - size / 2
        originY = screen.height / 2 - size / 2
    }

    /**
     * Update the background scope image
     */
    fun updateScope() {
        updateDimensions()
        updateMarkers()

        for (track in screen.radar.activeTracks()) {
            track.screenPoint = null
        }

        scopeImage = screen.createImage(size, size)
        val graphics = scopeImage?.createGraphics()

        graphics ?: return
        graphics.applyHDRenderingHints()

        graphics.renderingHints[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
        graphics.renderingHints[RenderingHints.KEY_TEXT_ANTIALIASING] = RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        graphics.renderingHints[RenderingHints.KEY_STROKE_CONTROL] = RenderingHints.VALUE_STROKE_PURE

        paintScopeBase(graphics)
        paintGaugeMarkings(graphics)

        graphics.dispose()
    }

    /**
     * Update the scope range markers image
     */
    private fun updateMarkers() {
        rangeImage = screen.createImage(diameter, diameter)
        val graphics = rangeImage?.createGraphics() as Graphics2D
        graphics.applyHDRenderingHints()

        if (SCOPE_RANGE_MARKER_INTERVAL > 0) {
            paintScopeRangeMarkers(graphics)
        }

        if (SCOPE_CURSOR_ENABLED) {
            paintScopeCursor(graphics)
        }
    }

    /**
     * Paint the scope oval
     * @param graphics Graphics2D
     */
    private fun paintScopeBase(graphics: Graphics2D) {
        //scope draw in green
        graphics.color = SCOPE_PRIMARY_COLOR
        graphics.stroke = BasicStroke(1.0F)

        //draw the scope circle
        graphics.drawOval(padding, padding, diameter, diameter)
    }

    /**
     * Paint the radar sweep line.
     * @param graphics Graphics2D
     */
    private fun paintSweepLine(graphics: Graphics2D) {
        if (SCOPE_SWEEP_RPM < 1) {
            return
        }

        //calculate length and angle of line
        val sweepLength = diameter / 2
        val sweepRotation = transformAngle(screen.radar.sweep.sweepRotation)

        graphics.color = SCOPE_PRIMARY_COLOR

        for (i in 0..5) {
            graphics.drawLine(center, center, sweepRotation + (i * 0.01), sweepLength)
        }
    }

    /**
     * Paint the scopes 'gauge' markings
     * @param graphics Graphics2D
     */
    private fun paintGaugeMarkings(graphics: Graphics2D) {
        graphics.font = FONT

        for (i in 0..359) {
            graphics.color = SCOPE_PRIMARY_COLOR.darker()

            //convert angle to radians, apply offset
            val angleRadians = transformAngle(Math.toRadians(i.toDouble()))

            //longer marker for bigger step intervals
            val markerLength = if (i % 10 == 0) 10 else 5

            //calculate starting point for marker
            val markerStart = calculatePoint(center, center, angleRadians, diameter / 2)

            //draw to end point of marker
            graphics.drawLine(markerStart.x.toInt(), markerStart.y.toInt(), angleRadians, markerLength)

            if (markerLength == 10) {
                graphics.color = SCOPE_PRIMARY_COLOR
                val lblPos = calculatePoint(markerStart.x.toInt(), markerStart.y.toInt(), angleRadians, 20)
                graphics.drawStringCentered(String.format("%03d", i), lblPos.x.toInt(), lblPos.y.toInt())
            }
        }
    }

    /**
     * Paint the scope cursor
     * @param graphics Graphics2D
     */
    private fun paintScopeCursor(graphics: Graphics2D) {
        //this image is rendered at an offset = to the padding, so take away the padding
        val relativeCenter = center - padding

        graphics.stroke = STROKE_DASHED
        graphics.drawLine(relativeCenter, relativeCenter, transformAngle(Math.toRadians(cursor)), diameter / 2)
        graphics.stroke = STROKE_DEFAULT
    }

    /**
     * Paint the scope range markers
     * @param graphics Graphics2D
     */
    private fun paintScopeRangeMarkers(graphics: Graphics2D) {
        graphics.color = SCOPE_PRIMARY_COLOR.darker()
        graphics.font = FONT

        //calculate center relative.
        val center = diameter / 2

        val visibleRangeKm = calculateVisibleRange().toInt() / 1000

        //in kilometres
        for (i in 0..visibleRangeKm step SCOPE_RANGE_MARKER_INTERVAL) {
            if (i == 0) continue

            val dist = (scale * i * 1000).toInt()

            //only draw maker if it's within range
            if (dist < center) {
                val origin = center - dist

                graphics.drawOval(origin, origin, dist * 2, dist * 2)
                graphics.drawString(i.toString() + "km", center - 8, origin - 5)
            }
        }
    }

    /**
     * Paint the radar tracks in the visible viewport.
     * @param graphics Graphics2D
     */
    private fun paintTracks(graphics: Graphics2D) {
        val composite = graphics.composite

        for (track in screen.radar.activeTracks()) {
            //dont paint track if it has no position
            if (!track.plottable) {
                continue
            }

            //check if the scale has changed and distance needs to be re-calculated.
            if (scaleChanged || track.screenDist == null) {
                track.screenDist = (track.realDist * scale).toInt()
            }

            val distance = track.screenDist ?: continue

            //check the track is not outside of the view port
            if (distance > diameter / 2) {
                continue
            }

            //bearing needs + 1/2Pi because of java
            val bearing = transformAngle(track.interrogBearing)

            if (track.screenPoint == null || scaleChanged) {
                track.screenPoint = calculatePoint(center, center, bearing, distance)
            }

            val screenPoint = track.screenPoint ?: continue
            val x = screenPoint.x.toInt()
            val y = screenPoint.y.toInt()

            if (track.screenCallout == null) {
                //re-draw the cached call out
                track.screenCallout = createTrackCallOut(track)
            }

            //calculate the track opacity based on the time elapsed since it was interrogated.
            if (SCOPE_FADE_TIME > 0) {
                val alpha = calculateTrackAlpha(track)
                if (alpha == 0f) {
                    continue
                }

                graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
            }

            //draw callout
            graphics.drawImage(track.screenCallout!!, x - 3, y - 6, null)
        }

        graphics.composite = composite
    }

    /**
     * Pre-draw the radar tracks call out.
     * @param track track to render callout for.
     */
    private fun createTrackCallOut(track: RadarTrack): BufferedImage {
        //centre track dot is at (x=3, y=6)
        //todo calc proper size
        val callOut = screen.createImage(70, 30)
        val graphics = callOut.createGraphics()
        graphics.applyHDRenderingHints()

        graphics.color = SCOPE_PRIMARY_COLOR
        graphics.font = FONT_TRACK

        if (!track.onGround) {
            graphics.fillOval(0, 2, 5, 5)
        }

        graphics.drawOval(0, 2, 5, 5)

        var y = 10
        val callSign = track.identity
        if (callSign != null) {
            graphics.drawString(callSign, 10, y)
            y += 12
        }

        val squawkCode = track.modeA
        if (squawkCode != null) {
            graphics.drawString(squawkCode, 10, y)
        }

        graphics.dispose()
        return callOut
    }

    /**
     * Calculate a tracks current alpha value based on time since it was interrogated.
     * @param track RadarTrack
     */
    private fun calculateTrackAlpha(track: RadarTrack): Float {
        val percentSweepRotation = track.elapsedSinceInterrogation() / (SCOPE_FADE_TIME.toFloat() * 1000)

        var alphaVal = 1 - percentSweepRotation

        alphaVal = alphaVal.coerceAtLeast(0.0f)
        alphaVal = alphaVal.coerceAtMost(1.0f)

        return alphaVal
    }

    /**
     * Fully paint the scope
     * @param graphics Graphics2D
     */
    fun paintScope(graphics: Graphics2D) {
        graphics.translate(originX, originY)

        //create background scope if it does not exist
        if (scopeImage == null || scopeChanged) {
            updateScope()
            scopeChanged = false
        }

        if (scaleChanged) {
            updateMarkers()
        }

        screen.drawImage(graphics, scopeImage, 0, 0)
        screen.drawImage(graphics, rangeImage, padding, padding)

        paintTracks(graphics)
        paintSweepLine(graphics)

        //reset this after, as track painting needs to know if the scale changed.
        scaleChanged = false
    }

    /**
     * Calculate the sweep re-paint bounds.
     *
     * Returns a clip of screen to be repainted, aims to improve performance
     * on low end machines by not having to repaint the whole screen
     *
     */
    fun calculateSweepBounds(): Rectangle {
        val end = calculatePoint(center, center, transformAngle(screen.radar.sweep.sweepRotation) + 0.05, (diameter / 2))

        val minX = end.x.toInt().coerceAtMost(center)
        val minY = end.y.toInt().coerceAtMost(center)
        val maxX = end.x.toInt().coerceAtLeast(center)
        val maxY = end.y.toInt().coerceAtLeast(center)

        val bounds = Rectangle(minX - 2, minY - 2, maxX - minX + 4, maxY - minY + 4)

        //apply transform
        bounds.translate(originX, originY)

        return bounds
    }

    /**
     * Calculate the re paint bounds for the specified radar track
     */
    fun calculateTrackBounds(track: RadarTrack): Rectangle? {
        //if either of these are null - no point re painting this area
        val point = track.screenPoint ?: return null
        val callOut = track.screenCallout ?: return null

        val bounds = Rectangle(point.x.toInt() - 4, point.y.toInt() - 4, callOut.width + 4, callOut.height + 4)
        bounds.translate(originX, originY)
        return bounds
    }

    private fun transformAngle(angle: Double): Double {
        return -angle + Math.PI + Math.toRadians(rotation)
    }

    /**
     * Calculate the diameter of the radar scope
     */
    private fun calculateDiameter(): Int {
        return calculateSize() - (padding * 2)
    }

    /**
     * Calculate the size for the radar scope
     */
    private fun calculateSize(): Int {
        return screen.width.coerceAtMost(screen.height)
    }

    /**
     * Calculate the center position of the radar scope
     */
    private fun calculateCenter(): Int {
        return (calculateSize() / 2)
    }

    /**
     * Calculate the visible range of the radar scope
     */
    fun calculateVisibleRange(): Double {
        return round((diameter / 2) / scale)
    }

    /**
     * Set the visible range of the radar scope
     */
    fun setVisibleRange(range: Double) {
        this.scale = diameter / (2 * range.coerceAtMost(250_000.0))
        this.scaleChanged = true
    }
}