package com.macgavrina.co_accounting

import android.app.Application
import android.content.Context
import com.macgavrina.co_accounting.rxjava.RxBus
import com.macgavrina.co_accounting.room.AppDatabase
import androidx.room.Room






//This class is necessary to get application context - MainApplication.applicationContext()

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        bus = RxBus()
        db = Room.databaseBuilder(applicationContext,
                AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()
    }

    companion object {

        lateinit var instance: MainApplication
            private set

        lateinit var bus:RxBus
            private set

        lateinit var db:AppDatabase
            private set

        fun applicationContext() : Context {
            return instance.applicationContext
        }

    }

}