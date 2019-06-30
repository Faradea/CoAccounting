package com.macgavrina.co_accounting.room

import androidx.room.*
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import com.macgavrina.co_accounting.support.STATUS_DELETED
import io.reactivex.Maybe

@Dao
interface ContactToTripRelationDAO {
    @Query("SELECT * FROM contacttotriprelation WHERE tripId IN (:tripId) AND status = $STATUS_ACTIVE")
    fun getAllContactsForTrip(tripId: Int): Maybe<List<ContactToTripRelation>>

    @Insert
    fun addContactToTripRelation(contactToTripRelation: ContactToTripRelation)

    @Query("UPDATE contacttotriprelation SET STATUS = $STATUS_DELETED WHERE contactId IN (:contactId) AND tripId IN (:tripId)")
    fun deleteContactToTripRelation(contactId: Int, tripId: Int)
}