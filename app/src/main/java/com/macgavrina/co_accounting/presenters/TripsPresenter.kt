package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.TripsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.disposables.Disposable

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
//        MainApplication.db.contactDAO().getAll("active")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : DisposableMaybeObserver<List<Contact>>() {
//                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
//                        Log.d("Contacts list is received from DB, size = ${contactsList.size}")
//                        getView()?.hideProgress()
//                        getView()?.initializeList(contactsList)
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.d("Error loading contacts from DB, $e")
//                        getView()?.hideProgress()
//                        getView()?.displayToast("Database error")
//                    }
//
//                    override fun onComplete() {
//                        getView()?.hideProgress()
//                    }
//                })
    }

}