package com.macgavrina.co_accounting.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Maybe

@Dao
interface TripDAO {
    @Query("SELECT * FROM trip WHERE status IN (:status)")
    fun getAllByStatus(status: String): Maybe<List<Trip>>

    @Query("SELECT * FROM trip WHERE uid IN (:tripId)")
    fun getTripByIds(tripId: String): Maybe<Trip>

    @Insert
    fun insertTrip(trip: Trip)

    @Query("UPDATE trip SET status = :status WHERE uid IN (:tripId)")
    fun deleteTrip(tripId: String, status: String)

    @Update
    fun updateTrip(trip:Trip)
}