package net.kitesoftware.scope1090.connection

import net.kitesoftware.scope1090.radar.Radar

/**
 * Created by niall on 21/05/2020.
 */
abstract class Connection {
    abstract fun connect(radar: Radar)
    abstract fun disconnect(radar: Radar)
}