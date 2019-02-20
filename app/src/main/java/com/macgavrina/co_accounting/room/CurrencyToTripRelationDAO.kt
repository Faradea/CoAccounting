package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Maybe

@Dao
interface CurrencyToTripRelationDAO {

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId from Currency " +
            "LEFT JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "AND (CurrencyToTripRelation.tripId IN (:tripId) OR CurrencyToTripRelation.tripId IS NULL)")
    fun getAllCurrenciesWithUsedForTripMarker(tripId: Int): LiveData<List<Currency>>

    @Query("SELECT COUNT (*) FROM currency")
    fun getAllCurrenciesListSize(): Int

    @Insert
    fun insertCurrency(currency: Currency)

    @Insert
    fun addCurrencyToTripRelation(currencyToTripRelation: CurrencyToTripRelation)

    @Query("DELETE FROM currencytotriprelation WHERE currencyId IN (:currencyId) AND tripId IN (:tripId)")
    fun deleteCurrencyToTripRelation(currencyId: Int, tripId: Int)

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId from Currency " +
            "INNER JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "WHERE tripId IN (:tripId)")
    fun getAllActiveCurrenciesForTrip(tripId: Int): LiveData<List<Currency>>

    @Query("select Currency.*, CurrencyToTripRelation.tripId as activeTripId, Trip.lastUsedCurrencyId from Currency " +
            "INNER JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "INNER JOIN Trip ON CurrencyToTripRelation.tripId = Trip.uid " +
            "WHERE Trip.isCurrent = 1")
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

}