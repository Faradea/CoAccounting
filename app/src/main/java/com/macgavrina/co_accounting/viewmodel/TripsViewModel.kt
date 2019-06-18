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
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
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

    private var selectedTripId: Int = -1

    private var repository: TripRepository = TripRepository()
    private var allTrips: LiveData<List<Trip>> = repository.getAll()
    private var selectedTrip: LiveData<Trip>? = null

    private var lastDeletedTrip: Trip? = null

    init {
        subscribeToEventBus()
    }

    fun getAll(): LiveData<List<Trip>> {
        return allTrips
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
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
//                            is Events.SetupLastUsedCurrency -> {
//                                Log.d("Catch Events.SetupLastUsedCurrency event, currencyId = ${`object`.currencyId}, currentTripId = $currentTripId")
//                                repository.setupLastUsedCurrencyForTrip(currentTripId, `object`.currencyId)
//                            }
                        }
        }
            compositeDisposable.add(subscriptionToBus)
    }

//    fun restoreLastDeletedTrip() {
//
//        if (lastDeletedTrip == null) return
//
//        repository.restoreDeletedTrip(lastDeletedTrip!!)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
//                    override fun onSubscribe(d: Disposable) {}
//
//                    override fun onComplete() {
//                        Log.d("Trip is restored")
//                        if (lastDeletedTrip != null && lastDeletedTrip!!.isCurrent) {
//                            updateClickedTripIsCurrentField(lastDeletedTrip!!.uid.toString(), lastDeletedTrip!!.isCurrent)
//                        }
//                        lastDeletedTrip = null
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.d("Error restoring trip, $e")
//                        toastMessage.value = "Database error"
//                    }
//                })
//    }

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