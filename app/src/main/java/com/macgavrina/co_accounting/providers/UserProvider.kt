package com.macgavrina.co_accounting.providers

import android.content.SharedPreferences
import com.macgavrina.co_accounting.model.User
import android.util.Log
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.preferences.MySharedPreferences

class UserProvider() {

    fun saveUserData(user:User) {
        val mySharedPreferences = MySharedPreferences(MainApplication.applicationContext())
        mySharedPreferences.login = user.login
        mySharedPreferences.token = user.token
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