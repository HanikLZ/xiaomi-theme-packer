package org.mdvsc.tools.xiaomi.skin.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author HanikLZ
 * @since 2016/12/1
 */
object DateTimeUtils {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    fun formatDateTime(date: Date, onlyDate: Boolean = false) = (if (onlyDate) dateFormat else dateTimeFormat).format(date)!!
    fun formatCurrentDateTime(offsetMillis: Long = 0, onlyDate: Boolean = false) = formatDateTime(Date(System.currentTimeMillis() + offsetMillis), onlyDate)

}

