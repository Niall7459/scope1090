package net.kitesoftware.scope1090

import java.awt.Color

/**
 * Created by niall on 18/09/2020.
 */

/**
 * Paint frames per second
 */
var SCOPE_FRAMES_PER_SECOND = 60

/**
 * Enable high quality rendering hints
 */
var SCOPE_HD = false

/**
 * Enable full-screen mode
 */
var SCOPE_FULLSCREEN = false

/**
 * Primary scope colour
 */
var SCOPE_PRIMARY_COLOR: Color = Color.GREEN

/**
 * Time in seconds for a track to fade to black
 */
var SCOPE_FADE_TIME = 2

/**
 * Scope sweep revolutions per minute. Recommended to stay below 60
 */
var SCOPE_SWEEP_RPM = 36

/**
 * Scope range marker interval
 */
var SCOPE_RANGE_MARKER_INTERVAL = 50

/**
 * Scope north cursor enabled
 */
var SCOPE_CURSOR_ENABLED = true

/**
 * Scope buttons enabled
 */
var SCOPE_BUTTONS_ENABLED = true

fun parseSettings(args: Array<String>) {
    for (arg in args) {
        when {
            arg begins "--fps=" -> {
                SCOPE_FRAMES_PER_SECOND = arg.split("=")[1].toInt()
            }
            arg == "--hd" -> {
                SCOPE_HD = true
            }
            arg begins "--color=" -> {
                SCOPE_PRIMARY_COLOR = Color.decode(arg.split("=")[1])
            }
            arg begins "--rpm=" -> {
                SCOPE_SWEEP_RPM = arg.split("=")[1].toInt()
            }
            arg begins "--fade=" -> {
                SCOPE_FADE_TIME = arg.split("=")[1].toInt()
            }
            arg begins "--nocursor" -> {
                SCOPE_CURSOR_ENABLED = false
            }
            arg begins "--nobuttons" -> {
                SCOPE_BUTTONS_ENABLED = false
            }
            arg begins "--markers=" -> {
                SCOPE_RANGE_MARKER_INTERVAL = arg.split("=")[1].toInt()
            }
            arg == "--fullscreen" -> {
                SCOPE_FULLSCREEN = true
            }
        }
    }
}

infix fun String.begins(beginning: String): Boolean {
    return this.startsWith(beginning)
}