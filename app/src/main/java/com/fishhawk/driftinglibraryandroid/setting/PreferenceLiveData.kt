package com.fishhawk.driftinglibraryandroid.setting

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