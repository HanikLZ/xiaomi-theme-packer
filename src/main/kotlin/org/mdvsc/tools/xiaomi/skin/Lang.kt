package org.mdvsc.tools.xiaomi.skin

import java.io.InputStreamReader
import java.util.*

/**
 * @author HanikLZ
 * @since 2016/12/14
 */
object Lang {
    const val APP_NAME = "app_name"
}

private val textProperties = ApplicationStarter::class.java.getResourceAsStream("/text/zh.properties").use {
    Properties().apply { try {load(InputStreamReader(it, Charsets.UTF_8))} catch (ignored : Exception) {} }
}

fun String.lang() = textProperties.getProperty(this, this) ?: this


