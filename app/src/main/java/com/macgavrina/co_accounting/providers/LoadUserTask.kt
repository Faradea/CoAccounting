package com.macgavrina.co_accounting.providers

import android.content.SharedPreferences
import com.macgavrina.co_accounting.providers.UserProvider.LoadUserCallback
import android.os.AsyncTask
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.model.User
import java.util.concurrent.TimeUnit


const val PREF_TOKEN = "token"
const val PREF_LOGIN = "login"
const val PREF_FILENAME = "preferences"

class LoadUsersTask(callback: LoadUserCallback) : AsyncTask<Void, Void, User>() {

    lateinit var myCallback: LoadUserCallback

    init{
        myCallback = callback
    }

    override fun doInBackground(vararg params: Void): User? {
        val sharedPreferences:SharedPreferences = MainApplication.applicationContext().getSharedPreferences(PREF_FILENAME, 0)
        val login: String = sharedPreferences.getString(PREF_LOGIN, "")
        val token: String = sharedPreferences.getString(PREF_TOKEN, "")
        TimeUnit.SECONDS.sleep(3);
            if (login.isEmpty() or token.isEmpty()) {
                return null
            }
            return User(login, token)
        }

    override fun onPostExecute(user: User) {
            myCallback.onLoad(user)
    }
}