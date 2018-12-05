package org.mdvsc.tools.xiaomi.skin.preference

/**
 * @author HanikLZ
 * @since 2016/11/22
 */

fun <T> java.util.Properties.getPreference(item: PreferenceItem<T>) = this[item.key]?.toString()?.let {
    try {
        when(item.defaultValue) {
            is Int -> it.toInt()
            is Long -> it.toLong()
            is Short -> it.toShort()
            is Byte -> it.toByte()
            is Float -> it.toFloat()
            is Double -> it.toDouble()
            is Boolean -> it.toBoolean()
            else -> it
        } as T
    } catch (ignored: Exception) {
        null
    }
} ?: item.defaultValue

fun <T> java.util.Properties.setPreference(item: PreferenceItem<T>, value: T?) = value?.let { this[item.key] = it.toString() } ?: remove(item.key)

