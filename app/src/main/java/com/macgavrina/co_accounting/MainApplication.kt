package com.macgavrina.co_accounting

import android.app.Application
import android.content.Context
import com.macgavrina.co_accounting.rxjava.RxBus




//This class is necessary to get application context - MainApplication.applicationContext()

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        bus = RxBus()
    }

    companion object {
        lateinit var instance: MainApplication
            private set

        lateinit var bus:RxBus
            private set

        fun applicationContext() : Context {
            return instance.applicationContext
        }

        fun bus(): RxBus? {
            return bus
        }

    }




}