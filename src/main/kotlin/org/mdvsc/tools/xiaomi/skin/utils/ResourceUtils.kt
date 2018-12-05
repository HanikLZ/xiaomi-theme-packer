package org.mdvsc.tools.xiaomi.skin.utils

/**
 * @author Home
 * @since 2016/11/20.
 */
object ResourceUtils {
    fun getResourceURL(path: String) = ResourceUtils::class.java.getResource(path)!!
    fun getResourceInputStream(path: String) = ResourceUtils::class.java.getResourceAsStream(path)!!
}