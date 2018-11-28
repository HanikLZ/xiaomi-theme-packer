package org.mdvsc.tools.xiaomi.skin.utils

import org.apache.commons.lang3.SystemUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream
import java.io.FileOutputStream
import java.io.BufferedOutputStream

private fun InputStream.forEach(action: (String) -> Boolean) = BufferedReader(InputStreamReader(this)).use {
    var end : Boolean
    do {
        val line = it.readLine()
        end = line == null
    } while (!end && !action(line))
    end
}

fun List<String>.execute(outputStream: OutputStream) = ProcessBuilder(this)
        .redirectErrorStream(true)
        .start().let { process ->
    val commandLine = fold(StringBuilder(), {v, e -> v.append(e).append(' ')}).trim()
    println("run command \"$commandLine\"")
    try {
        process.inputStream.copyTo(outputStream)
        val code = process.waitFor()
        if (code != 0) {
            println("fail, code = $code")
            false
        } else true
    } finally { process.destroy() }
}


fun List<String>.execute(messageCallback: ((String) -> Boolean) = {
    println(it)
    false
}) = ProcessBuilder(this)
        .redirectErrorStream(true)
        .start().let { process ->
    val commandLine = fold(StringBuilder(), {v, e -> v.append(e).append(' ')}).trim()
    println("run command \"$commandLine\"")
    try {
        if (process.inputStream.forEach(messageCallback)) {
            val code = process.waitFor()
            if (code != 0) {
                println("fail, code = $code")
                false
            } else true
        } else {
            println("cancelled.")
            false
        }
    } finally { process.destroy() }
}

fun outputExecutableBinary(resourcePath: String) : String {
    var extensions = ""
    val name = if (SystemUtils.IS_OS_MAC) {
        "/binary/mac/$resourcePath"
    } else if (SystemUtils.IS_OS_LINUX) {
        "/binary/linux/$resourcePath"
    } else {
        extensions = ".exe"
        "/binary/windows/$resourcePath"
    }
    val file = ResourceUtils.getResourceInputStream(name).use {
        File.createTempFile("${resourcePath.substringAfterLast('/')}_", extensions).apply {
            Files.copy(it, toPath(), StandardCopyOption.REPLACE_EXISTING)
            setExecutable(true)
            deleteOnExit()
        }
    }
    try {
        ResourceUtils.getResourceInputStream("$name-lib").use {
            val parentFile = file.parentFile
            ZipInputStream(BufferedInputStream(it)).use { input ->
                do {
                    input.nextEntry?.let { entry ->
                        val outFile = parentFile?.let { File(it, entry.name) } ?: File(name)
                        if (!outFile.exists()) {
                            outFile.apply {
                                parentFile?.mkdirs()
                                createNewFile()
                                deleteOnExit()
                            }
                        }
                        BufferedOutputStream(FileOutputStream(outFile)).use {
                            do {
                                val read = input.read()
                                if (read != -1) it.write(read) else break
                            } while (true)
                        }
                    } ?: break
                } while (true)
            }
        }
    } catch (ignored : Exception) { println("$name has no library.") }
    return file.absolutePath

}

