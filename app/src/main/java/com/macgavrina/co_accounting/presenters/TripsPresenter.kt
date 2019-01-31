package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.TripsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class TripsPresenter: BasePresenter<TripsContract.View>(), TripsContract.Presenter {

    private var subscriptionToBus: Disposable? = null
    private var lastDeletedTrip: Trip? = null
    private var tripsList: List<Trip>? = null

    override fun attachView(baseViewContract: TripsContract.View) {
        super.attachView(baseViewContract)
        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
    }

    override fun viewIsReady() {
        getAndDisplayAllTrips()
    }

    override fun addTripButtonIsPressed() {
        Log.d("Add new trip button is pressed")
        MainApplication.bus.send(Events.AddTrip())
    }

    override fun tripItemIsSelected(selectedTripId: Int) {
        Log.d("User clicked on contact, selectedTripId = $selectedTripId")
    }

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
//                            is Events.DeletedTripIsRestored -> {
//                                Log.d("catch Events.DeletedTripIsRestored event, updating contacts list...")
//                                getAndDisplayAllTrips()
//                            }
                            is Events.OnClickSwitchTripList -> {
                                Log.d("Catch Events.OnClickSwitchTripList, tripId = ${`object`.tripId}, isChecked = ${`object`.switchIsChecked}")
                                if (tripsList != null && tripsList!!.size > 1) {
                                    updateClickedTripIsCurrentField(`object`.tripId, `object`.switchIsChecked)
                                } else {
                                    if (!`object`.switchIsChecked) {
                                        getView()?.displayToast("The only one trip can't be deactivated")
                                        getAndDisplayAllTrips()
                                    }
                                }
                            }
                            is Events.TripIsUpdated -> {
                                Log.d("Catch Events.TripIsUpdated event")
                                if (`object`.isCurrent) {
                                    disableAllOtherTrips(`object`.tripId)
                                } else {
                                    checkIfThereIsCurrentTripInTheList(`object`.tripId)
                                }
                            }
                            is Events.TripIsDeleted -> {
                                Log.d("Catch Events.TripIsDeleted event")
                                if (`object`.trip.isCurrent) {
                                    checkIfThereIsCurrentTripInTheList(`object`.trip.uid.toString())
                                }
                                lastDeletedTrip = `object`.trip
                                getView()?.displayOnDeleteTripSnackBar()
                            }
                            is Events.TripIsAdded -> {
                                Log.d("Catch Events.TripIsAdded event")
                                if (`object`.trip.isCurrent) {
                                    getLastTripAndDisableAllOtherTrips()
                                }
                            }
                        }
                    }
        }
    }

    private fun unsubscribeFromEventBus() {
        if (subscriptionToBus != null) {
            subscriptionToBus?.dispose()
            subscriptionToBus = null
        }
    }

    private fun getAndDisplayAllTrips() {
        getView()?.showProgress()
        MainApplication.db.tripDAO().getAllByStatus("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Trip>>() {
                    override fun onSuccess(tripsList: List<Trip>) {
                        this@TripsPresenter.tripsList = tripsList
                        Log.d("Trips list is received from DB, size = ${tripsList.size}")
                        getView()?.hideProgress()
                        getView()?.initializeList(tripsList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error loading trips from DB, $e")
                        getView()?.hideProgress()
                        getView()?.displayToast("Database error")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                    }
                })
    }

    private fun updateClickedTripIsCurrentField(tripId: String, isCurrent: Boolean) {

        getView()?.showProgress()

        Completable.fromAction {
            MainApplication.db.tripDAO().updateTripIsCurrentField(tripId, isCurrent)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Trip's isCurrent field is updated")
                        if (isCurrent) {
                            disableAllOtherTrips(tripId)
                        } else {
                            checkIfThereIsCurrentTripInTheList(tripId)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating trip, $e")
                        getView()?.displayToast("Database error")
                        getView()?.hideProgress()
                    }
                })
    }

    private fun disableAllOtherTrips(tripId: String) {

        Completable.fromAction { MainApplication.db.tripDAO().disableAllTripsExcept(tripId, false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {
                        Log.d("All trips except chosen one are deactivated")
                        getAndDisplayAllTrips()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error deactivating trips, $e")
                        getView()?.displayToast("Database error")
                        getView()?.hideProgress()
                    }
                })
    }

    private fun checkIfThereIsCurrentTripInTheList(exceptTripId: String) {
        MainApplication.db.tripDAO().getLastTripByIsCurrentValue(true, "active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Trip>() {
                    override fun onSuccess(trip: Trip) {
                        Log.d("There is already one current trip in the list, nothing else to do")
                        getAndDisplayAllTrips()
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

    private fun setLastTripAsCurrent(exceptTripId: String) {
        MainApplication.db.tripDAO().getLastTripByIsCurrentValueExceptChosenTrip(false, "active", exceptTripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Trip>() {
                    override fun onSuccess(trip: Trip) {
                        updateTripIsCurrentField(trip.uid.toString(), true)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting last trip with isCurrent=false from DB, $e")
                    }

                    override fun onComplete() {
                        Log.d("There is no trips with isCurrent=false in DB, so set isCurrent=true for the only one trip")
                        getView()?.displayToast("The only one trip can't be deactivated")
                        updateTripIsCurrentField(exceptTripId, true)
                    }
                })
    }

    private fun updateTripIsCurrentField(tripId: String, isCurrent: Boolean) {
        Completable.fromAction {
            MainApplication.db.tripDAO().updateTripIsCurrentField(tripId, isCurrent)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Trip's isCurrent field is updated")
                        getAndDisplayAllTrips()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating trip, $e")
                        getView()?.displayToast("Database error")
                        getView()?.hideProgress()
                    }
                })
    }

    override fun undoDeleteTripButtonIsPressed() {
        Log.d("Undo delete button is pressed")
        if (lastDeletedTrip == null) return

        lastDeletedTrip!!.status = "active"
        Completable.fromAction {
            MainApplication.db.tripDAO().updateTrip(lastDeletedTrip!!)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Trip is restored")
                        if (lastDeletedTrip != null && lastDeletedTrip!!.isCurrent) {
                            updateClickedTripIsCurrentField(lastDeletedTrip!!.uid.toString(), lastDeletedTrip!!.isCurrent)
                        } else {
                            getAndDisplayAllTrips()
                        }
                        lastDeletedTrip = null
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error restoring trip, $e")
                    }
                })
    }

    private fun getLastTripAndDisableAllOtherTrips() {
        MainApplication.db.tripDAO().getLastTripIdForStatus("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Int>() {
                    override fun onSuccess(uid: Int) {
                        disableAllOtherTrips(uid.toString())
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting last trip from DB, $e")
                    }

                    override fun onComplete() {
                    }
                })
    }
}