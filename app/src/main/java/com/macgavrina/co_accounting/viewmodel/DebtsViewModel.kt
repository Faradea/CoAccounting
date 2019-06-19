package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.*
import com.macgavrina.co_accounting.room.*
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import kotlin.math.exp

const val EXPENSE_ID_KEY = "expenseId"

class DebtsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val snackbarMessage = SingleLiveEvent<String>()

    private var tripsList = MutableLiveData<List<Trip>>()

    private var allDebtsForCurrentTrip: LiveData<List<Debt>> = DebtRepository().getAllDebtsForCurrentTrip()

    init {
        compositeDisposable.add(TripRepository().getAllRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ tripsList ->
                    tripsList.forEach { trip ->
                        if (trip.isCurrent) {
                            this.tripsList.value = tripsList
                            return@subscribe
                        }
                    }
                    Log.d("There is no current list in the trips list")
                    setupCurrentTrip(tripsList)
                }, {error ->
                    Log.d("Error getting trips list, $error")
                })
        )
    }

    fun getAllDebtsForCurrentTrip(): LiveData<List<Debt>> {
        return allDebtsForCurrentTrip
    }

    fun addDebtButtonIsPressed() {
        MainApplication.bus.send(Events.AddDebt())
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
    }

    fun getAllTrips(): LiveData<List<Trip>> {
        return tripsList
    }

    fun tripIsChanged(tripTitle: String) {
        tripsList.value?.forEach { trip ->
            if (trip.title == tripTitle) {
                TripRepository().updateTripIsCurrentField(trip.uid.toString(), true)
                TripRepository().disableAllTripsExceptOne(trip.uid.toString())
            }
        }
    }

    private fun setupCurrentTrip(tripsList: List<Trip>) {
        if (tripsList.isNotEmpty()) {
            TripRepository().updateTripIsCurrentField(tripsList[0].uid.toString(), true)
            tripsList[0].isCurrent = true
            this.tripsList.value = tripsList
        }
    }
}