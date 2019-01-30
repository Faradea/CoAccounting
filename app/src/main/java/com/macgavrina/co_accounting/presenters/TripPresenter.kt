package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.TripContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Trip
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
            trip.startdate = getView()?.getStartDate()?.toLong()
            trip.enddate = getView()?.getEndDate()?.toLong()
            trip.isCurrent = getView()?.getSwitchStatus() ?: false
            trip.status = "active"

            Completable.fromAction {
                MainApplication.db.tripDAO().insertTrip(trip)
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {}

                        override fun onComplete() {
                            Log.d("Trip is added")
                            getView()?.hideProgress()
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
                            getView()?.displayTripData(trip.title, trip.startdate.toString(), trip.enddate.toString(), trip.isCurrent)
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

        deleteTrip(trip)
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
                        //ToDo
                        //MainApplication.bus.send(Events.ContactIsDeleted(contact))
                        getView()?.finishSelf()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error deleting trip, $e")
                    }
                })
    }

}