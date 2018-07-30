package com.macgavrina.co_accounting.providers

import android.os.AsyncTask
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.preferences.MySharedPreferences

class CheckIfUserTokenExistTask(callback: UserProvider.CheckIfUserTokenExistCallback) : AsyncTask<Void, Void, Boolean>() {

    val myCallback: UserProvider.CheckIfUserTokenExistCallback = callback

    override fun doInBackground(vararg params: Void): Boolean {
        val mySharedPreferences: MySharedPreferences = MySharedPreferences(MainApplication.applicationContext())
        val login = mySharedPreferences.login
        if (login.isEmpty()) {
            return false
        }
        return true
    }

    override fun onPostExecute(ifExist:Boolean) {
        myCallback.onLoad(ifExist)
    }
}