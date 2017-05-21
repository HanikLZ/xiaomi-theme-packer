package org.mdvsc.tools.xiaomi.skin.utils

import execute
import outputExecutableBinary
import java.io.File
import java.util.regex.Pattern

/**
 * @author HanikLZ
 * @since 2017/5/10
 */
object DeviceUtils {

    private val adbExecutablePath by lazy { outputExecutableBinary("adb") }
    private val pushFileProgressPattern = Pattern.compile("\\d+%")

    /*
        val commands = listOf(
                "adb"
                , "shell"
                , "am"
                , "start"
                , "-n"
                , "com.android.thememanager/com.android.thememanager.ApplyThemeForScreenshot"
                , "-e"
                , "\"theme_file_path\""
                , "\"/sdcard/MIUI/theme/MIUIThemeToolGenerated.mtz\""
                , "-e"
                , "\"api_called_from\""
                , "\"Themeeditor_MUYE_16_8_4_qianye\""
        )
     */
    fun openTcpip(port: Int) = listOf(adbExecutablePath, "tcpip", port.toString()).execute()

    fun connect(ip: String, port: Int) = listOf(adbExecutablePath, "connect", "$ip:$port").execute()

    fun devices() = mutableListOf<String>().also { results ->
        listOf(adbExecutablePath, "devices").execute({
            val line = it.split(' ', '\t').map { it.trim() }
            if (line.size == 2 && line[1] == "device") results.add(line.first())
            false
        })
    }

    fun pushFile(device: String, filePath: String, devicePath: String, progress: (Double) -> Boolean) = listOf(adbExecutablePath, "-s", device, "push", filePath, devicePath).execute {
        progress((pushFileProgressPattern.matcher(it).run { if (find()) it.substring(start(), end() - 1).toIntOrNull() else null } ?: -1) / 100.0)
    }

/*
    @Throws(IOException::class)
    fun pullFile(device: String, devicePath: String, targetPath: String) = listOf(adbExecutablePath, "-s", device, "pull", devicePath, targetPath).execute()
*/

    fun applyTheme(device: String, themePath: String, progress: (Double) -> Boolean): Boolean {
        val themeFile = File(themePath)
        val devicePath = "/sdcard/temp.${themeFile.extension}"
        return pushFile(device, themePath, devicePath, progress) && listOf(adbExecutablePath, "-s", device, "shell",
                "am", "start", "-n", "com.android.thememanager/com.android.thememanager.ApplyThemeForScreenshot",
                "-e", "\"theme_file_path\"", "\"$devicePath\"",
                "-e", "\"api_called_from\"", "\"Xiaomi-Theme\""
        ).execute()
    }
}

