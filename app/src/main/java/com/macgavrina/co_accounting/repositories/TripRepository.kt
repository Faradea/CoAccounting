package com.macgavrina.co_accounting.repositories

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.room.TripDAO
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import com.macgavrina.co_accounting.support.STATUS_DELETED
import com.macgavrina.co_accounting.support.STATUS_DRAFT
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.stream.Collectors

class TripRepository {

    private var tripDao: TripDAO = MainApplication.db.tripDAO()

    fun getAll(): LiveData<List<Trip>> {
        return tripDao.getAll(STATUS_ACTIVE)
    }

    fun getAllRx(): Single<List<Trip>> {
        return tripDao.getAllRx(STATUS_ACTIVE)
    }

    fun getLastActiveTripId(): Observable<Maybe<Int>> {
        Log.d("getting last active trip id....")
        return Observable.fromCallable { tripDao.getLastTripId(STATUS_ACTIVE) }
    }

    fun getCurrentTrip(): Maybe<Trip> {
        return tripDao.getLastTripByIsCurrentValue(true, STATUS_ACTIVE)
    }

    fun getCurrentTripLiveData(): LiveData<Trip> {
        return tripDao.getLastTripByIsCurrentValueLiveData(true, STATUS_ACTIVE)
    }

    fun getActiveTripsAmount(): Observable<Int> {
        return Observable.fromCallable { tripDao.getActiveTripsCount() }
    }

    fun restoreDeletedTrip(trip: Trip): Completable {
        trip.status = STATUS_ACTIVE
        return Completable.fromAction {
            tripDao.updateTrip(trip)
        }
    }

    fun getTripById(tripId: Int): LiveData<Trip> {
        return tripDao.getTripById(tripId)
        //Observable.fromCallable { tripDao.getTripById(tripId) }
    }

    fun getTripByIdRx(tripId: Int): Maybe<Trip> {
        return tripDao.getTripByIdRx(tripId)
    }

    fun getLastTripByIsCurrentValue(isCurrent: Boolean): Observable<Maybe<Trip>> {
        return Observable.fromCallable { tripDao.getLastTripByIsCurrentValue(isCurrent, STATUS_ACTIVE) }
    }

    fun getLastTripByIsCurrentValueExceptChosenTrip(isCurrent: Boolean, exceptTripId: String): Observable<Maybe<Trip>> {
        return Observable.fromCallable { tripDao.getLastTripByIsCurrentValueExceptChosenTrip(isCurrent, STATUS_ACTIVE, exceptTripId) }
    }

    fun insertTrip(trip: Trip): Completable {
        return Completable.fromAction {
            tripDao.insertTrip(trip)
        }
    }

    fun updateTrip(trip: Trip): Completable {
        return tripDao.updateTrip(trip)
    }

    fun deleteTrip(trip: Trip): Completable {
        return Completable.fromAction {
            tripDao.deleteTrip(trip.uid.toString(), STATUS_DELETED)
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
                        Log.d("Trip's isCurrent field is updated, isCurrent = $isCurrent")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating trip, $e")
                    }
                })
    }

    fun setupLastUsedCurrencyForTrip(tripId: Int, currencyId: Int) {
        Completable.fromAction {
            MainApplication.db.tripDAO().setupLastUsedCurrencyForTrip(tripId, currencyId)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Last used currency for trip is updated")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating last used currency for trip, $e")
                    }
                })
    }

    fun setupLastUsedCurrencyForCurrentTrip(currencyId: Int) {
        Completable.fromAction {
            MainApplication.db.tripDAO().setupLastUsedCurrencyForCurrentTrip(currencyId)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Last used currency for current trip is updated")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error updating last used currency for current trip, $e")
                    }
                })
    }

    fun getTripDraft(): LiveData<Trip> {
        return tripDao.getTripDraft()
    }

    fun getTripDraftRx(): Maybe<Trip> {
        return tripDao.getTripDraftRx()
    }

    fun createTripDraft(): Completable {
        val trip = Trip()
        trip.status = STATUS_DRAFT
        return Completable.fromAction {
            tripDao.insertTrip(trip)
        }
    }

}