package com.macgavrina.co_accounting

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log


//This class is necessary to get application context - MainApplication.applicationContext()

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MainApplication
            private set

        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }
}