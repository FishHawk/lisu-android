package com.fishhawk.lisu.util

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.nsd.NsdManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.base.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.ensurePermission(permission: String): Boolean {
    return checkPermission(permission).also { isGrant ->
        if (!isGrant) (findActivity() as BaseActivity).requestPermission(permission)
    }
}

fun Context.openWebPage(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

fun Context.saveImage(image: Bitmap, filename: String) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "Lisu"
                )
            }
            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw IOException("Failed to create new MediaStore record.")
            contentResolver.openOutputStream(uri)
        } else {
            if (!ensurePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                throw IOException("Do not have write permission.")
            val imagesDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Lisu"
            )
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val imageFile = File(imagesDir, "$filename.png")
            FileOutputStream(imageFile)
        }?.use {
            if (!image.compress(CompressFormat.PNG, 100, it))
                throw IOException("Failed to save bitmap.")
            toast(R.string.image_saved)
        } ?: throw IOException("Failed to open output stream.")
    } catch (e: Throwable) {
        toast(e)
    }
}

fun Context.saveImage(image: Drawable, filename: String) {
    saveImage(image.toBitmap(), filename)
}

fun Context.shareText(title: String, text: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(shareIntent, title))
}

fun Context.shareImage(title: String, image: Bitmap, filename: String) {
    val file = try {
        val outputFile = File(cacheDir, "$filename.png")
        val outPutStream = FileOutputStream(outputFile)
        image.compress(CompressFormat.PNG, 100, outPutStream)
        outPutStream.flush()
        outPutStream.close()
        outputFile
    } catch (e: Throwable) {
        return toast(e)
    }
    val uri = file.toUriCompat(this)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    startActivity(Intent.createChooser(shareIntent, title))
}

fun Context.shareImage(title: String, image: Drawable, filename: String) {
    shareImage(title, image.toBitmap(), filename)
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(resId: Int) = toast(getString(resId))

fun Context.toast(throwable: Throwable) =
    throwable.message?.let { toast(it) }
        ?: toast(R.string.unknown_error)

fun Context.copyToClipboard(text: String, hintResId: Int? = null) {
    val clip = ClipData.newPlainText("simple text", text)
    clipboardManager.setPrimaryClip(clip)
    hintResId?.let { toast(it) }
}

val Context.nsdManager: NsdManager
    get() = getSystemService(Context.NSD_SERVICE) as NsdManager

val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager