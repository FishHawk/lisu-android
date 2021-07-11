package com.fishhawk.driftinglibraryandroid.ui.base

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.ui.activity.BaseActivity
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

fun Fragment.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.ensurePermission(permission: String): Boolean {
    return checkPermission(permission).also { isGrant ->
        if (!isGrant) (requireActivity() as BaseActivity).requestPermission(permission)
    }
}

fun Fragment.saveImage(url: String, filename: String) {
    if (!ensurePermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) return
    lifecycleScope.launch(Dispatchers.Main) {
        try {
            val resolver: ContentResolver = requireContext().contentResolver
            val uri = resolver.run {
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

                insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
            } ?: return@launch requireContext().toast(R.string.toast_image_already_exist)

            withContext(Dispatchers.IO) {
                val outputStream = resolver.openOutputStream(uri)!!
                val bitmap = Glide.with(requireContext()).asBitmap().load(url).submit().get()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            }

            requireContext().toast(R.string.toast_image_saved)
        } catch (e: Throwable) {
            requireContext().toast(e)
        }
    }
}

fun CoroutineScope.shareImage(context: Context, url: String, filename: String) =
    launch(Dispatchers.IO) {
        try {
            val srcFile = Glide.with(context).asFile().load(url).submit().get()
            val dir = File(context.cacheDir, "shared_image")
            dir.mkdirs()
            val destFile = File(dir, "$filename.${srcFile.extension}")
            srcFile.copyTo(destFile, overwrite = true)

            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", destFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
        } catch (e: Throwable) {
            context.toast(e)
        }
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


