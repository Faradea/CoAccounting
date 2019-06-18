package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface TripDAO {
    @Query("SELECT * FROM trip WHERE status IN (:status) ORDER BY uid DESC")
    fun getAll(status: String): LiveData<List<Trip>>

    @Query("SELECT * FROM trip WHERE status IN (:status) ORDER BY uid DESC")
    fun getAllRx(status: String): Single<List<Trip>>

    @Query("SELECT COUNT (*) FROM trip WHERE status = \"active\"")
    fun getActiveTripsCount(): Int

    @Query("SELECT uid FROM trip WHERE status IN (:status) ORDER BY uid DESC LIMIT 1")
    fun getLastTripId(status: String): Maybe<Int>

    @Query("SELECT * FROM trip WHERE uid IN (:tripId)")
    fun getTripById(tripId: Int): LiveData<Trip>

    @Query("SELECT * FROM trip WHERE uid IN (:tripId)")
    fun getTripByIdRx(tripId: Int): Maybe<Trip>

    @Query("SELECT * FROM trip WHERE isCurrent IN (:isCurrent) AND status IN (:status) ORDER BY uid DESC LIMIT 1")
    fun getLastTripByIsCurrentValue(isCurrent: Boolean, status: String): Maybe<Trip>

    @Query("SELECT * FROM trip WHERE isCurrent IN (:isCurrent) AND status IN (:status) ORDER BY uid DESC LIMIT 1")
    fun getLastTripByIsCurrentValueLiveData(isCurrent: Boolean, status: String): LiveData<Trip>

    @Query("SELECT * FROM trip WHERE isCurrent IN (:isCurrent) AND status IN (:status) AND uid NOT IN (:exceptTripId) ORDER BY uid DESC LIMIT 1")
    fun getLastTripByIsCurrentValueExceptChosenTrip(isCurrent: Boolean, status: String, exceptTripId: String): Maybe<Trip>

    @Insert
    fun insertTrip(trip: Trip)

    @Update
    fun updateTrip(trip: Trip): Completable

    @Query("UPDATE trip SET status = :status, isCurrent = 0 WHERE uid IN (:tripId)")
    fun deleteTrip(tripId: String, status: String)

    @Query("UPDATE trip SET isCurrent = :isCurrent WHERE uid NOT IN (:tripId)")
    fun disableAllTripsExcept(tripId: String, isCurrent: Boolean)

    @Query("UPDATE trip SET isCurrent = :isCurrent WHERE uid IN (:tripId)")
    fun updateTripIsCurrentField(tripId: String, isCurrent: Boolean)

    @Query("UPDATE trip SET lastUsedCurrencyId = :currencyId WHERE uid IN (:tripId)")
    fun setupLastUsedCurrencyForTrip(tripId: Int, currencyId: Int)

    @Query("UPDATE trip SET lastUsedCurrencyId = :currencyId WHERE isCurrent = 1")
    fun setupLastUsedCurrencyForCurrentTrip(currencyId: Int)

    @Query("SELECT * FROM trip WHERE status = \"draft\" ORDER BY uid DESC LIMIT 1")
    fun getTripDraft(): LiveData<Trip>

    @Query("SELECT * FROM trip WHERE status = \"draft\" ORDER BY uid DESC LIMIT 1")
    fun getTripDraftRx(): Maybe<Trip>
}