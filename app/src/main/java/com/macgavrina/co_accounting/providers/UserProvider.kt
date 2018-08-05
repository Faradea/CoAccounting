package com.macgavrina.co_accounting.providers

import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.preferences.MySharedPreferences

class UserProvider() {

    fun saveUserData(user:User) {
        val mySharedPreferences = MySharedPreferences(MainApplication.applicationContext())
        mySharedPreferences.login = user.login
        mySharedPreferences.token = user.token
        Log.d("User login and token are saved in shared preferences")
    }

    fun clearUserData() {
        val mySharedPreferences = MySharedPreferences(MainApplication.applicationContext())
        mySharedPreferences.clear()
        Log.d( "User login and token are deleted")
    }


    fun loadUser(callback: LoadUserCallback){
        val loadUserTask = LoadUserTask(callback)
        loadUserTask.execute()
    }

    fun checkIfUserTokenExist(callback: CheckIfUserTokenExistCallback) {
        val checkIfUserTokenExistTask = CheckIfUserTokenExistTask(callback)
        checkIfUserTokenExistTask.execute()
    }

    interface LoadUserCallback {
        fun onLoad(user: User)
    }

    interface CheckIfUserTokenExistCallback {
        fun onLoad(ifExist: Boolean)
    }
}