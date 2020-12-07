package com.fishhawk.driftinglibraryandroid.util

import android.content.Context

fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density + 0.5f).toInt()


