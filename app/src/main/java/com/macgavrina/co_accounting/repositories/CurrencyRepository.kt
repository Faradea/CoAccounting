package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CurrencyRepository {

    private var currencyToTripRelationDAO: CurrencyToTripRelationDAO = MainApplication.db.currencyToTripRelationDAO()

    init {
    }

    fun getAllCurrenciesForTrip(tripId: Int): LiveData<List<Currency>> {
        return currencyToTripRelationDAO.getAllCurrenciesWithUsedForTripMarker(tripId)
    }

    fun getAllCurrenciesListSize(): Observable<Int> {
        return Observable.fromCallable { currencyToTripRelationDAO.getAllCurrenciesListSize() }
    }

    fun insertCurrency(currency: Currency) {
        Completable.fromAction {
            currencyToTripRelationDAO.insertCurrency(currency)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.d("Currency is added")
                }, { error ->
                    Log.d("Error adding currency into DB, $error")
                })
    }

    fun enableCurrencyForTrip(currencyId: Int, tripId: Int) {
        Completable.fromAction {
            currencyToTripRelationDAO.addCurrencyToTripRelation(CurrencyToTripRelation(currencyId, tripId))
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.d("Currency to trip relation is added")
                }, { error ->
                    Log.d("Error adding currency to trip relation, $error")
                })

    }

    fun disableCurrencyForTrip(currencyId: Int, tripId: Int) {
        Completable.fromAction {
            currencyToTripRelationDAO.deleteCurrencyToTripRelation(currencyId, tripId)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.d("Currency to trip relation is deleted")
                }, { error ->
                    Log.d("Error deleting currency to trip relation, $error")
                })
    }

    fun getAllActiveCurrenciesForTrip(tripId: Int): LiveData<List<Currency>> {
        return currencyToTripRelationDAO.getAllActiveCurrenciesForTrip(tripId)
    }

    fun getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip(): LiveData<List<Currency>> {
        return currencyToTripRelationDAO.getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip()
    }
}