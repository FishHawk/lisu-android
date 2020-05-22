package com.fishhawk.driftinglibraryandroid.util;

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Method

fun AppCompatActivity.getThemeResId(): Int {
    val wrapper: Class<*> = Context::class.java
    val method: Method = wrapper.getMethod("getThemeResId")
    method.isAccessible = true
    return method.invoke(this) as Int
}

