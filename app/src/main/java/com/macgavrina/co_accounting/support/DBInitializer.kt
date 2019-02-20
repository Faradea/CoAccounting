package com.macgavrina.co_accounting.support

import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Trip
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class DBInitializer() {

    init {
        checkIfAtLeastOneTripExists()
        checkIfCurrencyListIsInitialized()
    }

    private fun checkIfAtLeastOneTripExists() {
        TripRepository().getTripsAmount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({count ->
                    if (count == 0) {
                        createDefaultTrip()
                    }
                }, { error ->
                    Log.d("Error getting trips from DB, $error")
                })
    }

    private fun createDefaultTrip() {
        val newTrip = Trip()
        newTrip.title = "Unsorted"
        newTrip.status = "active"
        newTrip.isCurrent = true
        TripRepository().insertTrip(newTrip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    Log.d("New default trip is created")
                }, {e ->
                    Log.d("Error creating default trip, $e")
                })
    }

    private fun checkIfCurrencyListIsInitialized() {

        CurrencyRepository().getAllCurrenciesListSize()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({count ->
                    if (count == 0) {
                        initializeCurrenciesList()
                    }
                }, { error ->
                    Log.d("Error getting trips from DB, $error")
                })

    }

    private fun initializeCurrenciesList() {
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