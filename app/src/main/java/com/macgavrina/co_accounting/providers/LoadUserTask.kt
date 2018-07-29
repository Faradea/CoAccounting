package com.macgavrina.co_accounting.providers

import android.content.SharedPreferences
import com.macgavrina.co_accounting.providers.UserProvider.LoadUserCallback
import android.os.AsyncTask
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.preferences.MySharedPreferences
import java.util.concurrent.TimeUnit




class LoadUsersTask(callback: LoadUserCallback) : AsyncTask<Void, Void, User>() {

    lateinit var myCallback: LoadUserCallback

    init{
        myCallback = callback
    }

    override fun doInBackground(vararg params: Void): User? {
        val mySharedPreferences: MySharedPreferences = MySharedPreferences(MainApplication.applicationContext())
        val login = mySharedPreferences.login
        val token = mySharedPreferences.token
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