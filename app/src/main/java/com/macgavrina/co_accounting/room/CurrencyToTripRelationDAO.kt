package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Maybe

@Dao
interface CurrencyToTripRelationDAO {

    @Query("select Currency.*, CurrencyToTripRelation.tripId as isActiveForTrip from Currency LEFT JOIN CurrencyToTripRelation ON CurrencyToTripRelation.currencyId = Currency.uid " +
            "WHERE tripId IN (:tripId) OR tripId IS NULL")
    fun getAllCurrenciesWithUsedForTripMarker(tripId: Int): LiveData<List<Currency>>
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