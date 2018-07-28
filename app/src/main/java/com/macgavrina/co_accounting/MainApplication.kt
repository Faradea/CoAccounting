package com.macgavrina.co_accounting

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context


//This class is necessary to get application context - MainApplication.applicationContext()

@SuppressLint("Registered")
class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // initialize for any

        // Use ApplicationContext.
        // example: SharedPreferences etc...
        val context: Context = MainApplication.applicationContext()
    }
}