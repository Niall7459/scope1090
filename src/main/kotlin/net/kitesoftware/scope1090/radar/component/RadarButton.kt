package net.kitesoftware.scope1090.radar.component

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D

/**
 * Created by niall on 26/08/2020.
 */

enum class ButtonPosition {
    LEFT, RIGHT
}

class RadarButton(private val name: String,
                  private val active: Boolean,
                  private val position: ButtonPosition) {

    fun draw(graphics: Graphics2D, x: Int, y: Int) {
        if (active) {
            graphics.color = Color(0, 180, 0)
        } else {
            graphics.color = Color(0, 50, 0)
        }
        graphics.fillRoundRect(x, y, 50, 40, 5, 5)

        graphics.color = Color.GREEN
        graphics.drawRoundRect(x, y, 50, 40, 5, 5)
        if (position == ButtonPosition.LEFT) {
            graphics.drawLine(x + 50 + 10, y + 20, x + 50 + 20, y + 20)
        } else {
            graphics.drawLine(x - 20, y + 20, x - 10, y + 20)
        }

        graphics.font = Font("Arial", Font.PLAIN, 14)
        if (active) {
            graphics.color = Color.GREEN
        }

        if (position == ButtonPosition.LEFT) {
            graphics.drawString(name, x + 50 + 30, y + 25)
        } else {
            graphics.drawString(name, x - 10 - 30, y + 25)
        }
    }
}