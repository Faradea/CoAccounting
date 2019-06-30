package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import com.macgavrina.co_accounting.support.STATUS_DELETED
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface CurrencyToTripRelationDAO {

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId from Currency " +
            "LEFT JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "AND (CurrencyToTripRelation.tripId IN (:tripId) OR CurrencyToTripRelation.tripId IS NULL) " +
            "AND CurrencyToTripRelation.status = $STATUS_ACTIVE AND currency.status = $STATUS_ACTIVE")
    fun getAllCurrenciesWithUsedForTripMarker(tripId: Int): Single<List<Currency>>

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId from Currency " +
            "LEFT JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "AND (CurrencyToTripRelation.tripId IN (:tripId) OR CurrencyToTripRelation.tripId IS NULL) " +
            "AND CurrencyToTripRelation.status = $STATUS_ACTIVE AND currency.status = $STATUS_ACTIVE")
    fun getAllCurrenciesWithUsedForTripMarkerLiveData(tripId: Int): LiveData<List<Currency>>

    @Query("SELECT COUNT (*) FROM currency WHERE currency.status = $STATUS_ACTIVE")
    fun getAllCurrenciesListSize(): Int

    @Query("SELECT * FROM currency WHERE currency.status = $STATUS_ACTIVE")
    fun getAllCurrencies(): Single<List<Currency>>

    @Insert
    fun insertCurrency(currency: Currency)

    @Insert
    fun insertCurrencyWithIdReturned(currency: Currency): Long

    @Insert
    fun addCurrencyToTripRelation(currencyToTripRelation: CurrencyToTripRelation)

    @Query("UPDATE currencytotriprelation SET STATUS = $STATUS_DELETED WHERE currencyId IN (:currencyId) AND tripId IN (:tripId)")
    fun deleteCurrencyToTripRelation(currencyId: Int, tripId: Int)

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId from Currency " +
            "INNER JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "WHERE tripId IN (:tripId) AND CurrencyToTripRelation.status = $STATUS_ACTIVE AND currency.status = $STATUS_ACTIVE")
    fun getAllActiveCurrenciesForTrip(tripId: Int): LiveData<List<Currency>>

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId from Currency " +
            "INNER JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "WHERE tripId IN (:tripId) AND CurrencyToTripRelation.status = $STATUS_ACTIVE AND currency.status = $STATUS_ACTIVE")
    fun getAllActiveCurrenciesForTripRx(tripId: Int): Single<List<Currency>>

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId, Trip.lastUsedCurrencyId from Currency " +
            "INNER JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid AND CurrencyToTripRelation.status = $STATUS_ACTIVE " +
            "INNER JOIN Trip ON CurrencyToTripRelation.tripId = Trip.uid AND trip.status = $STATUS_ACTIVE " +
            "WHERE Trip.isCurrent = 1 AND currency.status = $STATUS_ACTIVE")
    fun getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip(): LiveData<List<Currency>>
//
//    @Query("SELECT * FROM currencytotriprelation")
//    fun getAllCurrenciesActiveForTrip(tripId: Int): Maybe<List<CurrencyToTripRelation>>

//    @Query("SELECT * FROM currencytotriprelation")
//    fun getAllCurrenciesActiveForCurrentTrip(): Maybe<List<CurrencyToTripRelation>>
//
//    @Query("SELECT * FROM currencytotriprelation")
//    fun activateCurrencyForTrip(status: String)
//
//    @Query("SELECT * FROM currencytotriprelation")
//    fun deactivateCurrencyForTrip(status: String)

    @Query("select count(*) FROM debt WHERE currencyId IN (:currencyId) AND tripId IN (:tripId) AND debt.status = $STATUS_ACTIVE")
    fun checkIfCurrencyIsUsedInTrip(currencyId: Int, tripId: Int): Single<Int>

}