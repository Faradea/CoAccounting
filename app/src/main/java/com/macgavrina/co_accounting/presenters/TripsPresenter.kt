package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.TripsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class TripsPresenter: BasePresenter<TripsContract.View>(), TripsContract.Presenter {

    private var subscriptionToBus: Disposable? = null

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

}