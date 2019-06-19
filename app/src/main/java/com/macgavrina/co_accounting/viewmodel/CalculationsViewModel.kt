package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.CalculationsRepository
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Calculation
import com.macgavrina.co_accounting.room.Trip
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CalculationsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    private var repository: CalculationsRepository = CalculationsRepository()
    private var allCalculationsForCurrentTrip: LiveData<List<Calculation>>
    private var tripsList = MutableLiveData<List<Trip>>()

    init {
        //subscribeToEventBus()
        allCalculationsForCurrentTrip = repository.getAllCalculationsForCurrentTrip()

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

    fun getAllCalculationsForCurrentTrip(): LiveData<List<Calculation>> {
        return allCalculationsForCurrentTrip
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