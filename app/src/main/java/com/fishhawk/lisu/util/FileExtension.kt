package com.fishhawk.lisu.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun File.toUriCompat(context: Context): Uri {
    return FileProvider.getUriForFile(context, context.packageName + ".provider", this)
}
