package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.support.DateFormatter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TripViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val snackbarMessage = SingleLiveEvent<String>()

    private var currentTrip = MutableLiveData<Trip>()
    private var currencies = MutableLiveData<List<Currency>>()
    private var allTripsAmount: Int = 0

    init {
        compositeDisposable.add(TripRepository().getActiveTripsAmount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({tripsAmount ->
                    allTripsAmount = tripsAmount
                }, {error ->
                    Log.d("Error getting trips amount")
                })
        )
    }

    fun tripIdIsReceivedFromActivity(tripId: Int) {
        compositeDisposable.add(TripRepository().getTripByIdRx(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({  trip ->
                    currentTrip.value = trip
                }, {error ->
                    Log.d("Error getting trip by id = $tripId, error = $error")
                }, {
                    Log.d("No trip with such id in DB, creating new one")
                    createTripDraft()
                })
        )

        compositeDisposable.add(CurrencyRepository().getAllActiveCurrenciesForTripRx(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ currenciesList ->
                    Log.d("Currencies active for trip is received from DB, size = ${currenciesList.size}, value = ${currenciesList}")
                    currencies.value = currenciesList
                }, { error ->
                    Log.d("Error getting currencies list for trip from DB, $error")
                })
        )
    }

    fun getCurrentTrip(): LiveData<Trip> {
        return currentTrip
    }

    fun getCurrencies(): LiveData<List<Currency>>? {
        return currencies
    }

    fun isTripCanBeDeleted(): Boolean {
        if (allTripsAmount > 1 ) return true
        return false
    }

    fun returnFromCurrenciesActivity() {
        Log.d("return from currencies activity")
        compositeDisposable.add(CurrencyRepository().getAllActiveCurrenciesForTripRx(currentTrip.value?.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ currenciesList ->
                    Log.d("Currencies active for trip is received from DB, size = ${currenciesList.size}, value = ${currenciesList}")
                    currencies.postValue(currenciesList)
                }, { error ->
                    Log.d("Error getting currencies list for trip from DB, $error")
                })
        )
    }

    fun deleteTrip() {
        if (currentTrip.value != null) {
            compositeDisposable.add(TripRepository().deleteTrip(currentTrip.value!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        Log.d("Trip is deleted")
                    }, {error ->
                        Log.d("Error updating trip, $error")
                    })
            )
        }
    }

    fun saveTrip() {

        if (currentTrip.value == null) return

        currentTrip.value?.status = "active"

        Log.d("Saving trip, newValue = ${currentTrip.value}")
        compositeDisposable.add(TripRepository().updateTrip(currentTrip.value!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Log.d("Trip is updated")
                }, {error ->
                    Log.d("Error updating trip, $error")
                })
        )
    }

    fun tripTitleIsChanged(newTitle: String) {
        currentTrip.value?.title = newTitle
    }

    fun startdateIsChanged(newValue: String) {
        currentTrip.value?.startdate = DateFormatter().getTimestampFromFormattedDate(newValue)
    }

    fun enddateIsChanged(newValue: String) {
        currentTrip.value?.enddate = DateFormatter().getTimestampFromFormattedDate(newValue)
    }
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun createTripDraft() {
        compositeDisposable.add(TripRepository().createTripDraft()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    getTripDraftAsCurrentTrip()
                }, {
                    Log.d("Error creating trip draft")
                })
        )
    }

    private fun getTripDraftAsCurrentTrip() {
        compositeDisposable.add(
                TripRepository().getTripDraftRx()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({trip ->
                            currentTrip.value = trip
                        }, {error ->
                            Log.d("Error getting trip draft, $error")
                        })
        )
    }
}