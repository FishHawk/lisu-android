package com.fishhawk.driftinglibraryandroid.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

object FileUtil {
    suspend fun downloadImage(context: Context, url: String): File =
        withContext(Dispatchers.IO) {
            Glide.with(context)
                .downloadOnly()
                .load(url)
                .submit()
                .get()
        }

    suspend fun saveImage(context: Context, url: String, filename: String) =
        withContext(Dispatchers.IO) {
            val srcFile = downloadImage(context, url)
            val type = ImageUtil.findImageType(FileInputStream(srcFile))
                ?: throw Exception("Not an image")

            val destDir = File(
                Environment.getExternalStorageDirectory().absolutePath +
                        File.separator + Environment.DIRECTORY_PICTURES +
                        File.separator + "DriftingLibrary"
            )
            destDir.mkdirs()

            val validFilename = buildValidFilename(filename)
            val destFile = File(destDir, "$validFilename.${type.extension}")

            srcFile.copyTo(destFile, overwrite = true)
            scanMedia(context, destFile)
        }

    private fun scanMedia(context: Context, file: File) {
        scanMedia(context, Uri.fromFile(file))
    }

    private fun scanMedia(context: Context, uri: Uri) {
        val action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        val mediaScanIntent = Intent(action)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }

    private fun buildValidFilename(origName: String): String {
        val name = origName.trim('.', ' ')
        if (name.isEmpty()) throw Exception("Empty filename")

        val builder = StringBuilder(name.length)
        name.forEach { c -> builder.append(if (isValidFatFilenameChar(c)) c else '_') }

        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit minus 15 reserved characters.
        return builder.toString().take(240)
    }

    private fun isValidFatFilenameChar(c: Char): Boolean {
        if (0x00.toChar() <= c && c <= 0x1f.toChar()) {
            return false
        }
        return when (c) {
            '"', '*', '/', ':', '<', '>', '?', '\\', '|', 0x7f.toChar() -> false
            else -> true
        }
    }
}