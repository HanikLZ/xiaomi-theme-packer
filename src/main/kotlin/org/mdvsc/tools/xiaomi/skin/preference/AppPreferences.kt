package org.mdvsc.tools.xiaomi.skin.preference

/**
 * @author HanikLZ
 * @since 2016/11/22
 */
object AppPreferences {
    val screenshotHistory = PreferenceItem("screenshot_history", "")
    val themeHistory = PreferenceItem("theme_history", "")
    val themeHistorySize = PreferenceItem("theme_history_size", 10)
    val alwaysOnTop = PreferenceItem("always_on_top", false)
    val deviceIp = PreferenceItem("device_ip", "")
    val devicePort = PreferenceItem("device_port", 5555)
}
