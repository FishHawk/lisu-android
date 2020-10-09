package com.fishhawk.driftinglibraryandroid.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileUtil {
    suspend fun downloadImage(context: Context, url: String): File =
        withContext(Dispatchers.IO) {
            Glide.with(context)
                .downloadOnly()
                .load(url)
                .submit()
                .get()
        }

    fun createImageInGallery(context: Context, filename: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/DriftingLibrary/"
                )
            } else {
                put(
                    MediaStore.MediaColumns.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + "/DriftingLibrary/${filename}.png"
                )
            }
        }

        val resolver: ContentResolver = context.contentResolver
        return resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )
    }

    suspend fun saveImage(context: Context, url: String, uri: Uri) =
        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = context.contentResolver
            val outputStream = resolver.openOutputStream(uri)!!
            val bitmap = Glide.with(context).asBitmap().load(url).submit().get()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }
}