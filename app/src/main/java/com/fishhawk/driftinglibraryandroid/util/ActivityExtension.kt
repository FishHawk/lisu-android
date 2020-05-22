package com.fishhawk.driftinglibraryandroid.util;

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import com.fishhawk.driftinglibraryandroid.MainActivity
import com.fishhawk.driftinglibraryandroid.gallery.GalleryActivity
import com.fishhawk.driftinglibraryandroid.reader.ReaderActivity
import com.fishhawk.driftinglibraryandroid.repository.data.MangaDetail
import java.lang.reflect.Method

fun AppCompatActivity.getThemeResId(): Int {
    val wrapper: Class<*> = Context::class.java
    val method: Method = wrapper.getMethod("getThemeResId")
    method.isAccessible = true
    return method.invoke(this) as Int
}

fun AppCompatActivity.navToGalleryActivity(
    id: String,
    title: String,
    thumb: String,
    imageView: ImageView
) {
    val bundle = bundleOf(
        "id" to id,
        "title" to title,
        "thumb" to thumb
    )

    val intent = Intent(this, GalleryActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)

//    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//        this,
//        imageView,
//        ViewCompat.getTransitionName(imageView)!!
//    )
//                startActivity(intent, options.toBundle())
}

fun AppCompatActivity.navToReaderActivity(
    id: String,
    collectionIndex: Int = 0,
    chapterIndex: Int = 0,
    pageIndex: Int = 0
) {
    val bundle = bundleOf(
        "id" to id,
        "collectionIndex" to collectionIndex,
        "chapterIndex" to chapterIndex,
        "pageIndex" to pageIndex
    )

    val intent = Intent(this, ReaderActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun AppCompatActivity.navToMainActivity(
    filter: String
) {
    val bundle = bundleOf("filter" to filter)
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtras(bundle)
    startActivity(intent)
}
