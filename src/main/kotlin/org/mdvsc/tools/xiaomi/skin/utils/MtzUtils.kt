package org.mdvsc.tools.xiaomi.skin.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.util.zip.ZipInputStream

/**
 * @author HanikLZ
 * @since 2017/5/10
 */
object MtzUtils {

    private val ignoreDirNames = listOf(".DS_Store")
    private val ignoreZipDirNames = listOf("wallpaper")

    fun uncompressMtz(mtzPath: String, targetPath: String, root: Boolean = true): Boolean {
        fun File.uncompress(input: InputStream) {
            if (!exists()) createNewFile()
            FileOutputStream(this).use { input.copyTo(it) }
        }

        var isZip = false
        FileInputStream(mtzPath).use {
            ZipInputStream(BufferedInputStream(it)).run {
                do {
                    val zipEntry = try {
                        nextEntry
                    } catch (ignored: Exception) {
                        return false
                    } ?: break
                    isZip = true
                    val file = File(targetPath, zipEntry.name).apply { parentFile?.mkdirs() }
                    if (zipEntry.isDirectory) file.mkdirs() else {
                        file.parentFile?.mkdirs()
                        if (root) {
                            val tempFile = File(file.parentFile, ".${file.name}")
                            tempFile.uncompress(this)
                            if (uncompressMtz(tempFile.absolutePath, file.absolutePath, false)) tempFile.delete() else tempFile.renameTo(file)
                        } else {
                            file.uncompress(this)
                        }
                    }
                    closeEntry()
                } while (true)
            }
        }
        return isZip
    }

    fun compressFileToMtz(resourcesPath: String, targetFilePath: String, progress: ((Double) -> Unit)? = null, fileNames: List<String>? = null) {
        val targetFile = File(targetFilePath).apply { parentFile?.mkdirs() }
        val tempDirectory = Files.createTempDirectory(null).toFile()
        val fileProcessor: (File) -> File = { if (it.name.toLowerCase().endsWith(".9.png")) AaptUtils.process9png(it, tempDirectory) ?: it else it }
        ZipOutputStream(BufferedOutputStream(FileOutputStream(targetFile))).use {
            val resourcesFile = File(resourcesPath)
            var totalFiles = 1
            var processedFiles = 0
            createCompressedFile(it, if (resourcesFile.isDirectory) {
                var fileList = if (fileNames == null || fileNames.isEmpty()) resourcesFile.listUsefulFiles() else resourcesFile.rootListFiles(fileNames)
                fileList.apply { totalFiles = size }.map {
                    progress?.invoke((++processedFiles).toDouble() / totalFiles.toDouble())
                    if (it.isDirectory && !ignoreZipDirNames.contains(it.name)) compressFile(it, File(tempDirectory, it.name), fileProcessor) else it
                }.toTypedArray()
            } else arrayOf(resourcesFile), fileProcessor = fileProcessor)
        }
        tempDirectory.listFiles().forEach { it.delete() }
        tempDirectory.delete()
    }

    private fun compressFile(file: File, targetFile: File, fileProcessor: ((File) -> File)?) = targetFile.apply {
        parentFile?.mkdirs()
        ZipOutputStream(BufferedOutputStream(FileOutputStream(this))).use {
            createCompressedFile(it, if (file.isDirectory) file.listUsefulFiles() else arrayOf(file), fileProcessor = fileProcessor)
        }
    }

    private fun createCompressedFile(out: ZipOutputStream, files: Array<File>, path: String = "", fileProcessor: ((File) -> File)?) {
        files.forEach {
            if (it.isDirectory) {
                val zipEntry = ZipEntry("$path${it.name}/")
                out.putNextEntry(zipEntry)
                createCompressedFile(out, it.listUsefulFiles(), zipEntry.name, fileProcessor)
            } else {
                val file = fileProcessor?.invoke(it) ?: it
                out.putNextEntry(ZipEntry("$path${file.name}"))
                val buffer = ByteArray(1024)
                FileInputStream(file).use {
                    do {
                        val readLen = it.read(buffer)
                        if (readLen > 0) out.write(buffer, 0, readLen)
                    } while (readLen > 0)
                }
            }
        }
    }

    private fun File.listUsefulFiles() = listFiles { _, name -> !ignoreDirNames.contains(name) }

    private fun File.rootListFiles(fileNames: List<String>) = listFiles { _, name -> "description.xml".equals(name) || fileNames.contains(name) }
}

