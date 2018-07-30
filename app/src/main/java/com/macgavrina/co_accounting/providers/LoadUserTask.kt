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
        val login = mySharedPreferences.login
        val token = mySharedPreferences.token
            if (login.isEmpty() or token.isEmpty()) {
                return User("", "")
            }
            return User(login, token)
        }

    override fun onPostExecute(user: User) {
            myCallback.onLoad(user)
    }
}