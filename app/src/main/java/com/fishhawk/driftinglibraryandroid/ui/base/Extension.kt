package com.fishhawk.driftinglibraryandroid.ui.base

import android.app.Activity
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap.CompressFormat
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import coil.imageLoader
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderActivity
import java.io.File
import java.io.FileOutputStream

fun Context.navToReaderActivity(
    id: String,
    providerId: String?,
    collectionIndex: Int = 0,
    chapterIndex: Int = 0,
    pageIndex: Int = 0
) {
    val bundle = bundleOf(
        "id" to id,
        "providerId" to providerId,
        "collectionIndex" to collectionIndex,
        "chapterIndex" to chapterIndex,
        "pageIndex" to pageIndex
    )

    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun Context.navToReaderActivity(
    detail: MangaDetail,
    collectionIndex: Int = 0,
    chapterIndex: Int = 0,
    pageIndex: Int = 0
) {
    val bundle = bundleOf(
        "detail" to detail,
        "collectionIndex" to collectionIndex,
        "chapterIndex" to chapterIndex,
        "pageIndex" to pageIndex
    )

    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

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

fun Context.saveImage(url: String, filename: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
        !ensurePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    ) return

    val imageLoader = imageLoader
    val request = ImageRequest.Builder(this)
        .data(url)
        .listener(onError = { _, e -> toast(e) })
        .target(onSuccess = {
            try {
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
                val uri = contentResolver.run {
                    insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                } ?: return@target toast(R.string.toast_image_already_exist)

                contentResolver.openFileDescriptor(uri, "w", null).use { pfd ->
                    val outputStream = FileOutputStream(pfd!!.fileDescriptor);
                    it.toBitmap().compress(CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    toast(R.string.toast_image_saved)
                }
            } catch (e: Throwable) {
                toast(e)
            }
        })
        .build()
    imageLoader.enqueue(request)
}

fun Context.shareImage(url: String, filename: String) {
    val imageLoader = imageLoader
    val request = ImageRequest.Builder(this)
        .data(url)
        .listener(onError = { _, e -> toast(e) })
        .target(onSuccess = {
            try {
                val dir = File(cacheDir, "shared_image")
                dir.mkdirs()

                val bitmap = it.toBitmap()
                val outputFile = File(dir, "$filename.png")
                val outPutStream = FileOutputStream(outputFile)
                bitmap.compress(CompressFormat.PNG, 100, outPutStream)
                outPutStream.flush()
                outPutStream.close()

                val uri = FileProvider.getUriForFile(
                    this, "${packageName}.fileprovider", outputFile
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                startActivity(Intent.createChooser(shareIntent, "Share image via"))
            } catch (e: Throwable) {
                toast(e)
            }
        })
        .build()
    imageLoader.enqueue(request)
}

private fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(resId: Int) = toast(getString(resId))

fun Context.toast(throwable: Throwable) =
    throwable.message?.let { toast(it) }
        ?: toast(R.string.toast_unknown_error)

val Context.clipboardManager
    get() = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

fun Context.copyToClipboard(text: String, hintResId: Int? = null) {
    val clip = ClipData.newPlainText("simple text", text)
    clipboardManager.setPrimaryClip(clip)
    hintResId?.let { toast(it) }
}

//val Fragment.inputMethodManager
//    get() = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//
//fun Fragment.closeInputMethod() {
//    inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
//}


