package com.ext.draggablerotationalcubelibrary

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate


open class InitApplication : Application() {

    companion object {
        const val NIGHT_MODE = "NIGHT_MODE"
        const val CUBE_MODE = "CUBE_MODE"

        @JvmStatic
        lateinit var instance: InitApplication
            private set
    }

    private var isNightModeEnabled: Boolean = false
    private var isCubeModeEnabled: Boolean = true


    override fun onCreate() {
        super.onCreate()
        instance = this

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        isNightModeEnabled = prefs.getBoolean(NIGHT_MODE, false)

        Log.e("TAG", "isNightModeEnabled = $isNightModeEnabled")

        AppCompatDelegate.setDefaultNightMode(
            if (isNightModeEnabled)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setIsCubeModeEnabled(enabled: Boolean) {
        isCubeModeEnabled = enabled
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(CUBE_MODE, enabled)
            .apply()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}
