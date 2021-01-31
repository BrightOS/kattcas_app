package ru.brightos.kattcasapp.data

import android.content.SharedPreferences

class PreferenceRepository(private val sharedPreferences: SharedPreferences) {
    var host: String
        get() = sharedPreferences.getString(HOST, " ")!!
        set(value) {
            value.let { sharedPreferences.edit().putString(HOST, it).apply() }
        }

    var user: String
        get() = sharedPreferences.getString(USER, " ")!!
        set(value) {
            value.let { sharedPreferences.edit().putString(USER, it).apply() }
        }

    var password: String
        get() = sharedPreferences.getString(PASSWORD, " ")!!
        set(value) {
            value.let { sharedPreferences.edit().putString(PASSWORD, it).apply() }
        }

    var localNickname: String
        get() = sharedPreferences.getString(LOCAL_NICKNAME, "Кацуша")!!
        set(value) {
            value.let { sharedPreferences.edit().putString(LOCAL_NICKNAME, it).apply() }
        }

    companion object {
        private const val HOST = "yViQhO5uBju6BgYc7jAxCaTa757IsSiVNoGfPMrV"
        private const val USER = "EIn4M42jcucjM5lMIiknkJZM2pHsFqDmRVkGevcw"
        private const val PASSWORD = "0VZJMWpPhXSo4kVu46MBrBBRSeOJvA1H2p2Mw8cq"
        private const val LOCAL_NICKNAME = "nWlrlX2tlDuahobGW4Ha5bFWdxqshqX4aFBUrmHq"
    }
}