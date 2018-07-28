package com.macgavrina.co_accounting.providers

import android.content.ComponentCallbacks
import android.content.Context
import android.content.SharedPreferences
import com.macgavrina.co_accounting.model.User
import java.security.AccessController.getContext
import java.util.prefs.Preferences
import android.widget.Toast
import android.content.Context.MODE_PRIVATE
import android.content.Context.MODE_PRIVATE
import android.os.AsyncTask.execute
import android.util.Log
import com.macgavrina.co_accounting.MainApplication

class UserProvider() {

    val sharedPreferences:SharedPreferences = MainApplication.applicationContext().getSharedPreferences(PREF_FILENAME, 0)

    fun saveUserData(user:User) {
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putString(PREF_LOGIN, user.login)
        sharedPreferencesEditor.putString(PREF_TOKEN, user.token)
        sharedPreferencesEditor.apply()
        Log.d("InDebtApp", "User login and token are saved in shared preferences")
    }


    fun loadUser(callback: LoadUserCallback){

        val loadUserTask = LoadUsersTask(callback)
        loadUserTask.execute()
    }

    interface LoadUserCallback {
        fun onLoad(user: User)
    }
}