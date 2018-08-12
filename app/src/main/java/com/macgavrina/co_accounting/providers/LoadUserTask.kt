package com.macgavrina.co_accounting.providers

import com.macgavrina.co_accounting.providers.UserProvider.LoadUserCallback
import android.os.AsyncTask
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.preferences.MySharedPreferences


class LoadUserTask(callback: LoadUserCallback) : AsyncTask<Void, Void, User>() {

    //ToDo использовать RxJava вместо AsyncTask

    val myCallback: LoadUserCallback = callback

    override fun doInBackground(vararg params: Void): User? {
        val mySharedPreferences: MySharedPreferences = MySharedPreferences(MainApplication.applicationContext())

        lateinit var login: String
        lateinit var token: String

        if (mySharedPreferences.login == null) {
            return User("", "")
        }

        if (mySharedPreferences.login!!.isEmpty()) {
            return User("", "")
        }

        if (mySharedPreferences.token == null) {
            return User("", "")
        }

        if (mySharedPreferences.token!!.isEmpty()) {
            return User("", "")
        }

        login = mySharedPreferences.login!!
        token = mySharedPreferences.token!!
        return User(login, token)

    }

    override fun onPostExecute(user: User) {
            myCallback.onLoad(user)
    }
}