package com.macgavrina.co_accounting.room

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.repositories.TripRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


@Database(entities = arrayOf(Contact::class, Debt::class, ReceiverWithAmountForDB::class, Expense::class, Trip::class, ContactToTripRelation::class, Currency::class, CurrencyToTripRelation::class), version = AppDatabase.DATABASE_VERSION)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDAO(): ContactDAO
    abstract fun debtDAO(): DebtDAO
    abstract fun receiverWithAmountForDBDAO(): ReceiverWithAmountForDBDAO
    abstract fun expenseDAO(): ExpenseDAO
    abstract fun tripDAO(): TripDAO
    abstract fun contactToTripRelationDAO(): ContactToTripRelationDAO
    abstract fun currencyToTripRelationDAO(): CurrencyToTripRelationDAO


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "InDebtDB"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "Sample.db")
                        // prepopulate the database after onCreate was called
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                Log.d("onCreateDB")
                                // insert the data on the IO Thread
                                initializeDataInDB()
                            }
                        })
                        .build()


        private fun initializeDataInDB() {

            createDefaultTrip()

            initializeCurrenciesList()
        }

        private fun createDefaultTrip() {
            val newTrip = Trip()
            newTrip.title = "Unsorted"
            newTrip.status = "active"
            newTrip.isCurrent = true

            TripRepository().insertTrip(newTrip)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        Log.d("Default trip is created")
                    }, {
                        Log.d("Error creating default trip, $it")
                    })
        }

        private fun initializeCurrenciesList() {
            Log.d("Initializing currencies list")

            val rurCurrency = Currency()
            rurCurrency.name = "RUR"
            rurCurrency.symbol = "\u20BD"
            CurrencyRepository().insertCurrency(rurCurrency)

            val eurCurrency = Currency()
            eurCurrency.name = "EUR"
            eurCurrency.symbol = "€"
            CurrencyRepository().insertCurrency(eurCurrency)

            val usdCurrency = Currency()
            usdCurrency.name = "USD"
            usdCurrency.symbol = "$"
            CurrencyRepository().insertCurrency(usdCurrency)
        }
    }
}