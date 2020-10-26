package com.fishhawk.driftinglibraryandroid.preference

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.MutableLiveData

abstract class PreferenceLiveData<T>(
    protected val sharedPreferences: SharedPreferences,
    private val key: String,
    protected val defaultValue: T
) : MutableLiveData<T>() {
    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _, key ->
        if (this.key == key) super.setValue(getPreference(key, defaultValue))
    }

    protected abstract fun getPreference(key: String, defValue: T): T
    protected abstract fun setPreference(key: String, value: T)

    fun getValueDirectly(): T = getPreference(key, defaultValue)

    override fun onActive() {
        super.onActive()
        super.setValue(getPreference(key, defaultValue))
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        super.onInactive()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun setValue(value: T) {
        setPreference(key, value)
    }
}

class PreferenceEnumLiveData<T : Enum<T>>(
    sharedPrefs: SharedPreferences,
    key: String,
    defaultValue: T,
    private val clazz: Class<T>
) : PreferenceLiveData<T>(sharedPrefs, key, defaultValue) {
    override fun getPreference(key: String, defValue: T): T {
        val enumName = sharedPreferences.getString(key, null)
        return clazz.enumConstants?.find { it.name == enumName } ?: defaultValue
    }

    override fun setPreference(key: String, value: T) =
        sharedPreferences.edit().putString(key, value.name).apply()

    fun setValue(ordinal: Int) = setValue(clazz.enumConstants?.getOrNull(ordinal) ?: defaultValue)
    fun getOrdinal() = getValueDirectly().ordinal

    fun setNextValue() {
        val current = getValueDirectly()
        val next = clazz.enumConstants?.let { it[(current.ordinal + 1) % it.size] } ?: defaultValue
        setValue(next)
    }
}

class PreferenceStringLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    defaultValue: String
) : PreferenceLiveData<String>(sharedPrefs, key, defaultValue) {
    override fun getPreference(key: String, defValue: String): String =
        sharedPreferences.getString(key, null) ?: defaultValue

    override fun setPreference(key: String, value: String) =
        sharedPreferences.edit().putString(key, value).apply()
}

class PreferenceIntLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    defaultValue: Int
) : PreferenceLiveData<Int>(sharedPrefs, key, defaultValue) {
    override fun getPreference(key: String, defValue: Int): Int =
        sharedPreferences.getInt(key, defaultValue)

    override fun setPreference(key: String, value: Int) =
        sharedPreferences.edit().putInt(key, value).apply()
}

class PreferenceBooleanLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    defaultValue: Boolean
) : PreferenceLiveData<Boolean>(sharedPrefs, key, defaultValue) {
    override fun getPreference(key: String, defValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    override fun setPreference(key: String, value: Boolean) =
        sharedPreferences.edit().putBoolean(key, value).apply()
}
