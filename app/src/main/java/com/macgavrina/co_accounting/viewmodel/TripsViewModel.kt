package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class TripsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val snackbarMessage = SingleLiveEvent<String>()

    private var repository: TripRepository = TripRepository()
    private var allTrips: LiveData<List<Trip>> = repository.getAll()

    private var lastDeletedTrip: Trip? = null

    init {
        subscribeToEventBus()
    }

    fun getAll(): LiveData<List<Trip>> {
        return allTrips
    }

    fun getTripById(tripId: String): LiveData<Trip> {
        return repository.getTripById(tripId)
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
    }

    fun insertTrip(trip: Trip) {
        val subscription = repository.insertTrip(trip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    if (trip.isCurrent) {
                        repository.getLastActiveTripId()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe ({ maybeInt ->
                                    maybeInt
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe ({ tripId ->
                                                repository.disableAllTripsExceptOne(tripId.toString())
                                            }, {error ->
                                                Log.d("Error disabling all trips except inserted one, $error")
                                                toastMessage.value = "Database error"
                                            })
                                        }, {error ->
                                            Log.d("Error getting last active trip id, $error")
                                            toastMessage.value = "Database error"
                                        })
                    }
                }, {error ->
                    Log.d("Error inserting trip, $error")
                    toastMessage.value = "Database error"
                })
        compositeDisposable.add(subscription)
    }

    fun updateTrip(trip: Trip) {
        val subscription = repository.updateTrip(trip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    if (trip.isCurrent) {
                        disableAllTripsExceptOne(trip.uid.toString())
                    } else {
                        checkIfThereIsCurrentTripInTheList(trip.uid.toString())
                    }
                }, {error ->
                    Log.d("Error updating trip, $error")
                    toastMessage.value = "Database error"
                })
        compositeDisposable.add(subscription)
    }

    fun deleteTrip(trip: Trip) {
        if (allTrips.value?.size == 1) {
            toastMessage.value = "The only one trip in the list can't be deleted"
            return
        } else {
            lastDeletedTrip = trip
            snackbarMessage.value = "Trip is deleted"
            val subscription = repository.deleteTrip(trip)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe ({
                        if (trip.isCurrent) {
                            checkIfThereIsCurrentTripInTheList(trip.uid.toString())
                        }
                    }, {error ->
                        Log.d("Error deleting trip, $error")
                        toastMessage.value = "Database error"
                    })
            compositeDisposable.add(subscription)
        }
    }

    fun disableAllTripsExceptOne(tripId: String) {
        repository.disableAllTripsExceptOne(tripId)
    }

    fun addTripButtonIsPressed() {
        MainApplication.bus.send(Events.AddTrip())
    }

        private fun subscribeToEventBus() {

            val subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.OnClickSwitchTripList -> {
                                Log.d("Catch Events.OnClickSwitchTripList, tripId = ${`object`.tripId}, isChecked = ${`object`.switchIsChecked}")
                                updateClickedTripIsCurrentField(`object`.tripId, `object`.switchIsChecked)
                            }
                        }
        }
            compositeDisposable.add(subscriptionToBus)
    }

    fun restoreLastDeletedTrip() {

        if (lastDeletedTrip == null) return

        repository.restoreDeletedTrip(lastDeletedTrip!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Trip is restored")
                        if (lastDeletedTrip != null && lastDeletedTrip!!.isCurrent) {
                            updateClickedTripIsCurrentField(lastDeletedTrip!!.uid.toString(), lastDeletedTrip!!.isCurrent)
                        }
                        lastDeletedTrip = null
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error restoring trip, $e")
                        toastMessage.value = "Database error"
                    }
                })
    }

    private fun updateClickedTripIsCurrentField(tripId: String, isCurrent: Boolean) {

        Log.d("updateClickedTripIsCurrentField, tripId = $tripId, isCurrent = $isCurrent")

        repository.updateTripIsCurrentField(tripId, isCurrent)

        if (!isCurrent && allTrips.value?.size == 1) {
            Log.d("updateClickedTripIsCurrentField, isCurrent == false but there is only one trip in the list")
            toastMessage.value = "The only one trip in the list can't be deactivated"
            repository.updateTripIsCurrentField(tripId, true)
            return
        }

        if (isCurrent) {
            repository.disableAllTripsExceptOne(tripId)
        } else {
            checkIfThereIsCurrentTripInTheList(tripId)
        }
    }

    private fun checkIfThereIsCurrentTripInTheList(exceptTripId: String) {

            val subscription = repository.getLastTripByIsCurrentValue(true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { maybeObservable ->
                        maybeObservable
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : DisposableMaybeObserver<Trip>() {
                                    override fun onSuccess(trip: Trip) {
                                    Log.d("There is already one current trip in the list, nothing else to do")
                                }

                                override fun onError(e: Throwable) {
                                    Log.d("Error getting trip with isCurrent=true from DB, $e")
                                    toastMessage.value = "Database error"
                                }

                                override fun onComplete() {
                                    Log.d("There is no current trip in DB, so setup the last added as current")

                                    setLastTripAsCurrent(exceptTripId)
                                }
                                })
                    }
        compositeDisposable.add(subscription)
    }

    private fun setLastTripAsCurrent(exceptTripId: String) {
        val subscription = repository.getLastTripByIsCurrentValueExceptChosenTrip(false, exceptTripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { maybeObservable ->
                    maybeObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : DisposableMaybeObserver<Trip>() {
                                override fun onSuccess(trip: Trip) {
                                    repository.updateTripIsCurrentField(trip.uid.toString(), true)
                                }

                                override fun onError(e: Throwable) {
                                    Log.d("Error getting last trip with isCurrent=false from DB, $e")
                                    toastMessage.value = "Database error"
                                }

                                override fun onComplete() {
                                    Log.d("There is no trips with isCurrent=false in DB, so set isCurrent=true for the only one trip")
                                    toastMessage.value = "The only one trip in the list can't be deactivated"
                                    repository.updateTripIsCurrentField(exceptTripId, true)
                                }
                            })

                }
        compositeDisposable.add(subscription)
    }
}