package org.mdvsc.tools.xiaomi.skin.preference

import java.util.*

/**
 * @author HanikLZ
 * @since 2016/11/22
 */
data class PreferenceItem<T>(val key: String, val defaultValue: T) {
    fun get(properties: Properties) = properties.getPreference(this)
    fun set(properties: Properties, value: T) { properties.setPreference(this, value) }
}

