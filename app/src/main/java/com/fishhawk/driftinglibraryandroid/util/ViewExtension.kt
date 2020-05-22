package com.fishhawk.driftinglibraryandroid.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.makeSnackBar(content: String) {
    Snackbar.make(this, content, Snackbar.LENGTH_SHORT).show()
}
