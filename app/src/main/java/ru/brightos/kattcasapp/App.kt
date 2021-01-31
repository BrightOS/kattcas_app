package ru.brightos.kattcasapp

import android.app.Application
import ru.brightos.kattcasapp.data.PreferenceRepository

class App : Application() {
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        preferenceRepository = PreferenceRepository(
            getSharedPreferences(DEFAULT_PREFERENCES, MODE_PRIVATE)
        )
    }

    companion object {
        const val DEFAULT_PREFERENCES = "u1mGzwxag10RarRhhsU5A1zwhwIaBWRyqJRReAr0"
    }
}