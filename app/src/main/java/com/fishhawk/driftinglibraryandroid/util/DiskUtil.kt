package com.fishhawk.driftinglibraryandroid.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object DiskUtil {
    fun scanMedia(context: Context, file: File) {
        scanMedia(context, Uri.fromFile(file))
    }

    fun scanMedia(context: Context, uri: Uri) {
        val action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        val mediaScanIntent = Intent(action)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }

    suspend fun saveImage(context: Context, url: String, filename: String) {
        withContext(Dispatchers.IO) {
            val file = Glide.with(context)
                .downloadOnly()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get()

            val type =
                ImageUtil.findImageType(FileInputStream(file)) ?: throw Exception("Not an image")

            val destDir = File(
                Environment.getExternalStorageDirectory().absolutePath +
                        File.separator + Environment.DIRECTORY_PICTURES +
                        File.separator + "DriftingLibrary"
            )
            destDir.mkdirs()

            val destFile = File(destDir, "$filename.${type.extension}")
            file.copyTo(destFile, overwrite = true)
            scanMedia(context, destFile)
        }
    }
}