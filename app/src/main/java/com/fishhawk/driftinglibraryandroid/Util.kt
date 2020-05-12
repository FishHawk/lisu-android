package com.fishhawk.driftinglibraryandroid

import android.content.Context
import java.lang.reflect.Method

object Util {
    fun extractThemeResId(context: Context): Int {
        val wrapper: Class<*> = Context::class.java
        val method: Method = wrapper.getMethod("getThemeResId")
        method.isAccessible = true
        return method.invoke(context) as Int
    }
}