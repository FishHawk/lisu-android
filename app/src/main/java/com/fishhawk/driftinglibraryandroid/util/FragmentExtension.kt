package com.fishhawk.driftinglibraryandroid.util

import androidx.fragment.app.Fragment
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.base.EmptyFetchMoreResultError
import com.fishhawk.driftinglibraryandroid.base.EmptyRefreshResultError


fun Fragment.showErrorMessage(throwable: Throwable) {
    val message = when (throwable) {
        is EmptyRefreshResultError -> getString(R.string.error_hint_empty_refresh_result)
        is EmptyFetchMoreResultError -> getString(R.string.error_hint_empty_fetch_more_result)
        else -> throwable.message ?: getString(R.string.library_unknown_error_hint)
    }
    view?.makeSnackBar(message)
}