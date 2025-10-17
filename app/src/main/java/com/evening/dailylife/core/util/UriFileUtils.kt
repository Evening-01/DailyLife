package com.evening.dailylife.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

/**
 * 与 Uri 及外部存储相关的文件工具方法。
 */
object UriFileUtils {

    fun uriToAbsolutePath(uri: Uri): String {
        val uriPath = uri.pathSegments?.get(uri.pathSegments!!.size - 1).toString()
        return if (uri.toString().contains("com.android.externalstorage.documents")) {
            if (uriPath.contains("primary")) {
                uriPath.replace("primary:", "/storage/emulated/0/")
            } else {
                "/storage/${uriPath.split(":")[0]}/${uriPath.split(":")[1]}"
            }
        } else if (uri.toString().contains("raw:")) {
            uriPath.substring(uriPath.indexOf("raw:") + 4)
        } else {
            ""
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result = ""
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result.isBlank()) {
            result = uri.path ?: ""
            val cut = result.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    fun fromUriCopyFileToExternalFilesDir(context: Context, uri: Uri, filename: String): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputFile = File(context.getExternalFilesDir(null), filename)
        val outputStream = FileOutputStream(outputFile, false)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return outputFile.absolutePath
    }

    fun copyFilesToExternalFilesDir(
        uri: Uri,
        context: Context,
        dirName: String,
        clean: Boolean = false
    ): String {
        val directory = DocumentFile.fromTreeUri(context, uri)
        val files = directory?.listFiles()
        if (clean) {
            val externalFilesDir =
                File("${context.getExternalFilesDir(null)?.absolutePath}/${dirName}")
            if (externalFilesDir.isDirectory)
                externalFilesDir.listFiles()?.forEach { file ->
                    file.delete()
                }
        }

        files?.forEach { file ->
            if (file.isFile) {
                val inputStream: InputStream? = context.contentResolver.openInputStream(file.uri)
                val outputFile = File(
                    "${context.getExternalFilesDir(null)?.absolutePath}/${dirName}",
                    file.name ?: ""
                )
                if (outputFile.parentFile?.exists() == false) {
                    outputFile.parentFile?.mkdirs()
                }
                val outputStream: OutputStream = FileOutputStream(outputFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        return directory?.name ?: ""
    }

    fun deleteOldFiles(context: Context) {
        val directory = context.getExternalFilesDir(null)
        val currentTime = System.currentTimeMillis()

        directory?.listFiles()?.forEach { file ->
            val fileAge = currentTime - file.lastModified()
            val fileAgeInDays = TimeUnit.MILLISECONDS.toDays(fileAge)

            if (fileAgeInDays > 3) {
                file.deleteRecursively()
            }
        }
    }

    fun readLastNChars(file: File, count: Int): String {
        val raf = RandomAccessFile(file, "r")
        val length = raf.length()
        raf.seek(length - count)
        val lastChars = raf.readLine()
        raf.close()
        return lastChars
    }

    fun copyAssetFileToExternalFilesDir(context: Context, filename: String) {
        val assetManager = context.assets

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = assetManager.open(filename)
            val outFile = File(context.getExternalFilesDir(null), filename)
            if (outFile.exists()) {
                outFile.delete()
            }
            outputStream = FileOutputStream(outFile, false)

            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}
