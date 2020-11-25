package com.fishhawk.driftinglibraryandroid.util

import com.tfcporciuncula.flow.Preference

inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

inline fun <reified T : Enum<T>> Preference<T>.setNext() = set(get().next())