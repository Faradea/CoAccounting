package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface ContactDAO {
    @Query("SELECT * FROM contact WHERE status IN (:status)")
    fun getAll(status: String): Maybe<List<Contact>>

    @Query("SELECT * FROM contact WHERE uid IN (:contactId)")
    fun getContactByIds(contactId: String): LiveData<Contact>

    @Insert
    fun insertContact(contact: Contact)

    @Query("UPDATE contact SET status = \"deleted\" WHERE uid IN (:contactId)")
    fun deleteContact(contactId: Int)

    @Update
    fun updateContact(contact:Contact)

//    @Query("SELECT contact.* FROM trip INNER JOIN contacttotriprelation ON trip.uid = contacttotriprelation.tripId INNER JOIN contact ON contacttotriprelation.contactId = contact.uid WHERE trip.isCurrent = 1 AND trip.status = \"active\" AND contact.status = \"active\"")
//    fun getContactsForCurrentTrip(): LiveData<List<Contact>>

    @Query("SELECT contact.*, MAX(Trip.isCurrent) as isActiveForCurrentTrip from contact LEFT JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid LEFT JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = \"active\" GROUP BY contact.uid")
    fun getContactsForCurrentTrip(): LiveData<List<Contact>>

    @Query("SELECT contact.*, Trip.isCurrent as isActiveForCurrentTrip from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = \"active\" AND trip.isCurrent = 1")
    fun getActiveContactsForCurrentTrip(): LiveData<List<Contact>>

    @Query("SELECT contact.*, Trip.isCurrent as isActiveForCurrentTrip from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = \"active\" AND trip.isCurrent = 1")
    fun getActiveContactsForCurrentTripRx(): Maybe<List<Contact>>

}