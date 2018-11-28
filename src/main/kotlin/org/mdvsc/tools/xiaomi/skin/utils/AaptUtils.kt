package org.mdvsc.tools.xiaomi.skin.utils

import java.io.File

object AaptUtils {

    private val aaptExecutablePath by lazy { outputExecutableBinary("aapt") }

    fun process9png(file: File, outputDir: File) = File(outputDir, file.name).let {
        listOf(aaptExecutablePath, "s", "-i", file.absolutePath, "-o", it.absolutePath).execute()
        if (it.exists()) it else null
    }

}

