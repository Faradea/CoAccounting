package com.macgavrina.co_accounting.room

import android.arch.persistence.room.RoomDatabase
import com.macgavrina.co_accounting.model.User
import android.arch.persistence.room.Database



@Database(entities = arrayOf(Contact::class, Debt::class), version = AppDatabase.DATABASE_VERSION)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDAO(): ContactDAO
    abstract fun debtDAO(): DebtDAO

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MainDB"
    }
}