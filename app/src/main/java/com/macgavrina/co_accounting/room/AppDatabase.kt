package com.macgavrina.co_accounting.room

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


@Database(entities = arrayOf(Contact::class, Debt::class, ReceiverWithAmountForDB::class, Expense::class, Trip::class, ContactToTripRelation::class, Currency::class, CurrencyToTripRelation::class, SenderWithAmount::class, SyncEvent::class), version = AppDatabase.DATABASE_VERSION)
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
                        MainApplication.bus.send(Events.DefaultTripIsCreated())

                        TripRepository().getCurrentTrip()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({trip ->
                                    initializeCurrenciesList(trip.uid)
                                }, {error ->
                                    Log.d("Error getting default trip, $error")
                                })
                    }, {
                        Log.d("Error creating default trip, $it")
                    })
        }

        private fun initializeCurrenciesList(tripId: Int) {
            Log.d("Initializing currencies list")

            val rurCurrency = Currency()
            rurCurrency.name = "RUR"
            rurCurrency.symbol = "\u20BD"
                CurrencyRepository().insertCurrencyWithIdReturned(rurCurrency)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ currencyId ->
                        addCurrencyToTripRelation(currencyId.toInt(), tripId)
                    }, {
                        Log.d("Error adding RUR currency")
                    })

            val eurCurrency = Currency()
            eurCurrency.name = "EUR"
            eurCurrency.symbol = "€"
            CurrencyRepository().insertCurrencyWithIdReturned(eurCurrency)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ currencyId ->
                        addCurrencyToTripRelation(currencyId.toInt(), tripId)
                    }, {
                        Log.d("Error adding RUR currency")
                    })


            val usdCurrency = Currency()
            usdCurrency.name = "USD"
            usdCurrency.symbol = "$"
            CurrencyRepository().insertCurrencyWithIdReturned(usdCurrency)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ currencyId ->
                        addCurrencyToTripRelation(currencyId.toInt(), tripId)
                    }, {
                        Log.d("Error adding RUR currency")
                    })
        }

        private fun addCurrencyToTripRelation(currencyId: Int, tripId: Int) {
            Completable.fromAction {
                CurrencyRepository().enableCurrencyForTrip(currencyId, tripId)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        Log.d("Currency (id = $currencyId) to trip (id = $tripId) relation is added")
                    }, {error ->
                        Log.d("Error enabling currency for trip, $error")
                    })
        }
    }
}