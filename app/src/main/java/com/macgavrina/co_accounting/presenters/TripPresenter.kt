package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.TripContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class TripPresenter: BasePresenter<TripContract.View>(), TripContract.Presenter {

    lateinit var trip: Trip

    override fun viewIsReady() {
        getView()?.hideProgress()
    }

    override fun doneButtonIsPressed() {

        Log.d("Done button is pressed")

        getView()?.hideKeyboard()

        getView()?.showProgress()

        if (::trip.isInitialized) {
            trip.title = getView()?.getTripTitle()
            trip.startdate = getView()?.getStartDate()?.toLong()
            trip.enddate = getView()?.getEndDate()?.toLong()
            trip.isCurrent = getView()?.getSwitchStatus() ?: false
            trip.status = "active"

            Completable.fromAction { MainApplication.db.tripDAO().updateTrip(trip) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onComplete() {
                            Log.d("Trip is updated")
                            MainApplication.bus.send(Events.TripIsUpdated(trip.uid.toString(), trip.isCurrent))
                            getView()?.finishSelf()
                        }

                        override fun onError(e: Throwable) {
                            Log.d("Error updating trip, $e")
                            getView()?.displayToast("Database error")
                            getView()?.hideProgress()
                        }
                    })

        } else {
            trip = Trip()
            trip.title = getView()?.getTripTitle()
            trip.startdate = getView()?.getStartDate()
            trip.enddate = getView()?.getEndDate()
            trip.isCurrent = getView()?.getSwitchStatus() ?: false
            trip.status = "active"

            Completable.fromAction {
                MainApplication.db.tripDAO().insertTrip(trip)
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {}

                        override fun onComplete() {
                            Log.d("Trip is added")
                            MainApplication.bus.send(Events.TripIsAdded(trip))
                            getView()?.finishSelf()
                        }

                        override fun onError(e: Throwable) {
                            Log.d("Error adding trip, $e")
                            getView()?.displayToast("Database error")
                            getView()?.hideProgress()
                        }
                    })
        }
    }

    override fun tripIdIsReceiverFromMainActivity(tripId: String?) {

        Log.d("TripId is received from main activity, = $tripId")

        if (tripId != null) {

            MainApplication.db.tripDAO().getTripByIds(tripId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableMaybeObserver<Trip>() {
                        override fun onSuccess(loadedTrip: Trip) {
                            trip = loadedTrip
                            getView()?.hideProgress()
                            getView()?.displayTripData(trip.title, trip.startdate, trip.enddate, trip.isCurrent)
                        }

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                        }
                    })
        } else {
            getView()?.hideDeleteButton()
        }
    }

    override fun deleteButtonIsPressed() {

        Log.d("Delete button is pressed")

        MainApplication.db.tripDAO().getAllByStatus("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Trip>>() {
                    override fun onSuccess(tripsList: List<Trip>) {
                        Log.d("Trips list is received from DB, size = ${tripsList.size}")
                        if (tripsList.size > 1) {
                            deleteTrip(trip)
                        } else {
                            getView()?.displayAlertDialog("You have only one trip - it can't be deleted")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error loading trips from DB, $e")
                        getView()?.displayToast("Database error")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                    }
                })
    }



    private fun deleteTrip(trip: Trip) {

        Completable.fromAction {
            MainApplication.db.tripDAO().deleteTrip(trip.uid.toString(), "deleted") }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onComplete() {
                        Log.d("Trip is deleted")
                        MainApplication.bus.send(Events.TripIsDeleted(trip))
                        getView()?.finishSelf()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error deleting trip, $e")
                    }
                })
    }

}