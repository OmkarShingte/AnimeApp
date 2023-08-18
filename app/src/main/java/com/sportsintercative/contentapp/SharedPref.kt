package com.sportsintercative.contentapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object SharedPref {
    private var sharedPref: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPref == null)
            sharedPref =
                context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    fun read(key: String, defValue: String): String? {
        return sharedPref!!.getString(key, defValue)
    }

    fun read(key: String, defValue: Boolean): Boolean {
        return sharedPref!!.getBoolean(key, defValue)
    }

    fun write(key: String, value: String) {
        val prefsEditor = sharedPref!!.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun write(key: String, value: Boolean) {
        val prefsEditor = sharedPref!!.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }
}