package net.kitesoftware.scope1090.radar

import java.awt.event.InputEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

/**
 * Created by niall on 21/05/2020.
 */
class RadarMouseListener(private val screen: RadarScreen) : MouseWheelListener {
    override fun mouseWheelMoved(event: MouseWheelEvent) {
        //using shift caused false positives
        val ctrlDown = event.modifiersEx and InputEvent.CTRL_DOWN_MASK != 0
        val wheelRotation = event.preciseWheelRotation

        event.consume()

        if (ctrlDown) {
            screen.scope.rotation += wheelRotation
            when {
                screen.scope.rotation > 360 -> screen.scope.rotation = screen.scope.rotation - 360.0
                screen.scope.rotation < 0 -> screen.scope.rotation = 360.0 + screen.scope.rotation
            }

            screen.scope.scopeChanged = true
        } else {
            var scale = screen.scope.scale

            scale = if (wheelRotation > 0) {
                scale / (1.0 + wheelRotation * 0.025)
            } else {
                scale * (1.0 - wheelRotation * 0.025)
            }

            screen.scope.scale = scale
            screen.scope.scaleChanged = true
        }
    }
}