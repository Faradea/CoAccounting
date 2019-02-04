package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class TripsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    internal val toastMessage = SingleLiveEvent<String>()

    private var repository: TripRepository = TripRepository(application)
    private var allTrips: LiveData<List<Trip>> = repository.getAll()

    var onlyOneTripInTheList = MutableLiveData<Boolean?>()
    var errorDeactivationLastTrip = MutableLiveData<Boolean?>()

    init {
        subscribeToEventBus()
        if (allTrips.value?.size == 1) {
            onlyOneTripInTheList.value = true
        }
    }

    fun getAll(): LiveData<List<Trip>> {
        return allTrips
    }

    fun getLastDeletedTripId() {
        return repository.getLastDeletedTripId()
    }

    fun getTripById(tripId: String): LiveData<Trip> {
        return repository.getTripById(tripId)
    }

    fun getLastTripByIsCurrentValue(isCurrent: Boolean): Observable<Maybe<Trip>> {
        return repository.getLastTripByIsCurrentValue(isCurrent)
    }

    fun getLastTripByIsCurrentValueExceptChosenTrip(isCurrent: Boolean, exceptTripId: String): Observable<Maybe<Trip>> {
        return repository.getLastTripByIsCurrentValueExceptChosenTrip(isCurrent, exceptTripId)
    }

    fun insertTrip(trip: Trip) {
        repository.insertTrip(trip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    if (trip.isCurrent) {
                        repository.getLastActiveTripId()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe { maybeInt ->
                                    maybeInt
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe { tripId ->
                                                Log.d("Id of added trip = $tripId")
                                                repository.disableAllTripsExceptOne(tripId.toString())

                                            }
                                        }
                    }
                }
    }

    fun updateTrip(trip: Trip) {
        repository.updateTrip(trip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    if (trip.isCurrent) {
                        disableAllTripsExceptOne(trip.uid.toString())
                    } else {
                        checkIfThereIsCurrentTripInTheList(trip.uid.toString())
                    }
                }

    }

    fun deleteTrip(trip: Trip) {
        Log.d("deleteTrip, list size = ${allTrips.value?.size}")
        if (allTrips.value?.size == 1) {
            Log.d("Only one trip in the list")
            toastMessage.value = "The only one trip in the list can't be deleted"
            return
        } else {
            repository.deleteTrip(trip)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        if (trip.isCurrent) {
                            checkIfThereIsCurrentTripInTheList(trip.uid.toString())
                        }
                    }
        }
    }

    fun disableAllTripsExceptOne(tripId: String) {
        repository.disableAllTripsExceptOne(tripId)
    }

//    fun updateTripIsCurrentField(tripId: String, isCurrent: Boolean) {
//        repository.updateTripIsCurrentField(tripId, isCurrent)
//    }


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


//                                if (tripsList != null && tripsList!!.size > 1) {
//                                    updateClickedTripIsCurrentField(`object`.tripId, `object`.switchIsChecked)
//                                } else {
//                                    if (!`object`.switchIsChecked) {
//                                        getView()?.displayToast("The only one trip can't be deactivated")
//                                        getAndDisplayAllTrips()
//                                    }
//                                }
                            }
//                            is Events.TripIsUpdated -> {
//                                Log.d("Catch Events.TripIsUpdated event")
//                                if (`object`.isCurrent) {
//                                    disableAllOtherTrips(`object`.tripId)
//                                } else {
//                                    checkIfThereIsCurrentTripInTheList(`object`.tripId)
//                                }
//                            }
//                            is Events.TripIsDeleted -> {
//                                Log.d("Catch Events.TripIsDeleted event")
//                                if (`object`.trip.isCurrent) {
//                                    checkIfThereIsCurrentTripInTheList(`object`.trip.uid.toString())
//                                }
//                                lastDeletedTrip = `object`.trip
//                                getView()?.displayOnDeleteTripSnackBar()
//                            }
//                            is Events.TripIsAdded -> {
//                                Log.d("Catch Events.TripIsAdded event")
//                                if (`object`.trip.isCurrent) {
//                                    getLastTripAndDisableAllOtherTrips()
//                                }
//                            }
                        }
        }
    }

    private fun updateClickedTripIsCurrentField(tripId: String, isCurrent: Boolean) {

        repository.updateTripIsCurrentField(tripId, isCurrent)

        if (allTrips.value?.size == 1) {
            Log.d("list size = ${allTrips.value?.size}")
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

            repository.getLastTripByIsCurrentValue(true)
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
                                }

                                override fun onComplete() {
                                    Log.d("There is no current trip in DB, so setup the last added as current")

                                    setLastTripAsCurrent(exceptTripId)
                                }
                                })
                    }
    }

    private fun setLastTripAsCurrent(exceptTripId: String) {
        repository.getLastTripByIsCurrentValueExceptChosenTrip(false, exceptTripId)
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
                                }

                                override fun onComplete() {
                                    Log.d("There is no trips with isCurrent=false in DB, so set isCurrent=true for the only one trip")
                                    toastMessage.value = "The only one trip in the list can't be deactivated"
                                    repository.updateTripIsCurrentField(exceptTripId, true)
                                }
                            })

                }
    }
}