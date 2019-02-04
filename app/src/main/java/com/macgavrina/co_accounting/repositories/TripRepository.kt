package com.macgavrina.co_accounting.repositories

import android.app.Application
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.room.TripDAO
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class TripRepository(application: Application) {

    private var tripDao: TripDAO = MainApplication.db.tripDAO()

    private var allTrips: LiveData<List<Trip>>

    init {
        allTrips = tripDao.getAll("active")
    }


    fun getAll(): LiveData<List<Trip>> {
        return allTrips
    }

    fun getLastActiveTripId(): Observable<Maybe<Int>> {
        Log.d("getting last active trip id....")
        return Observable.fromCallable { tripDao.getLastTripId("active") }
    }

    fun getLastDeletedTripId() {

    }

    fun getTripById(tripId: String): LiveData<Trip> {
        return tripDao.getTripById(tripId)
        //Observable.fromCallable { tripDao.getTripById(tripId) }
    }

    fun getLastTripByIsCurrentValue(isCurrent: Boolean): Observable<Maybe<Trip>> {
        return Observable.fromCallable { tripDao.getLastTripByIsCurrentValue(isCurrent, "active") }
    }

    fun getLastTripByIsCurrentValueExceptChosenTrip(isCurrent: Boolean, exceptTripId: String): Observable<Maybe<Trip>> {
        return Observable.fromCallable { tripDao.getLastTripByIsCurrentValueExceptChosenTrip(isCurrent, "active", exceptTripId) }
    }

    fun insertTrip(trip: Trip): Completable {
        return Completable.fromAction {
            tripDao.insertTrip(trip)
        }
    }

    fun updateTrip(trip: Trip): Completable {
        return Completable.fromAction {
            tripDao.updateTrip(trip)
        }
    }

    fun deleteTrip(trip: Trip): Completable {
        return Completable.fromAction {
            tripDao.deleteTrip(trip.uid.toString(), "deleted")
        }
    }

    fun disableAllTripsExceptOne(tripId: String) {
        Completable.fromAction {
            tripDao.disableAllTripsExcept(tripId, false)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Log.d("All trips except selected one are disabled")
                }

    }

    fun updateTripIsCurrentField(tripId: String, isCurrent: Boolean) {

        Completable.fromAction {
            MainApplication.db.tripDAO().updateTripIsCurrentField(tripId, isCurrent)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Trip's isCurrent field is updated")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating trip, $e")
                    }
                })
    }

}