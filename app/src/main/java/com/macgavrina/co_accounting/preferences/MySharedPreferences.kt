package com.macgavrina.co_accounting.preferences

import android.content.Context
import android.content.SharedPreferences


const val FILE_NAME = "preferences"

const val PREF_TOKEN = "token"

const val PREF_LOGIN = "login"

class MySharedPreferences(context: Context) {

    private val preferences: SharedPreferences

    private val editor: SharedPreferences.Editor
        get() = preferences.edit()

    var token: String?
        get() = preferences.getString(PREF_TOKEN, "")
        set(data) {
            editor.putString(PREF_TOKEN, data).commit()

        }

    var login: String?
        get() = preferences.getString(PREF_LOGIN, "")
        set(data) {
            editor.putString(PREF_LOGIN, data).commit()
        }

    fun clear() {
        editor.remove(PREF_TOKEN).remove(PREF_LOGIN).commit()
    }

    init {
        preferences = context.getSharedPreferences(FILE_NAME, 0)
    }
}