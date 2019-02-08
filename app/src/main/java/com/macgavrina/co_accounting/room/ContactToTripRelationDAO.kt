package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe

@Dao
interface ContactToTripRelationDAO {
    @Query("SELECT * FROM contacttotriprelation WHERE tripId IN (:tripId)")
    fun getAllContactsForTrip(tripId: Int): Maybe<List<ContactToTripRelation>>

    @Insert
    fun addContactToTripRelation(contactToTripRelation: ContactToTripRelation)

    @Query("DELETE FROM contacttotriprelation WHERE contactId IN (:contactId) AND tripId IN (:tripId)")
    fun deleteContactToTripRelation(contactId: Int, tripId: Int)
}