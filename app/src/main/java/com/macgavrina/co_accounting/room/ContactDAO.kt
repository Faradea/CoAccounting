package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import com.macgavrina.co_accounting.support.STATUS_DELETED
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ContactDAO {
    @Query("SELECT * FROM contact WHERE status IN (:status)")
    fun getAll(status: String): Maybe<List<Contact>>

    @Query("SELECT COUNT(*) FROM contact WHERE status = $STATUS_ACTIVE")
    fun getAllActiveContactsCountLiveData(): LiveData<Int>

    @Query("SELECT * FROM contact WHERE uid IN (:contactId)")
    fun getContactByIds(contactId: String): LiveData<Contact>

    @Query("SELECT * FROM contact WHERE uid IN (:contactId)")
    fun getContactByIdRx(contactId: Int): Maybe<Contact>

    @Insert
    fun insertContact(contact: Contact)

    @Query("UPDATE contact SET status = $STATUS_DELETED WHERE uid IN (:contactId)")
    fun deleteContact(contactId: Int)

    @Update
    fun updateContact(contact:Contact)

//    @Query("SELECT contact.* FROM trip INNER JOIN contacttotriprelation ON trip.uid = contacttotriprelation.tripId INNER JOIN contact ON contacttotriprelation.contactId = contact.uid WHERE trip.isCurrent = 1 AND trip.status = \"active\" AND contact.status = \"active\"")
//    fun getContactsForCurrentTrip(): LiveData<List<Contact>>

    @Query("SELECT contact.*, MAX(Trip.isCurrent) as isActiveForCurrentTrip from contact LEFT JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE LEFT JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = $STATUS_ACTIVE GROUP BY contact.uid")
    fun getContactsForCurrentTrip(): LiveData<List<Contact>>

    @Query("SELECT contact.*, MAX(Trip.uid = :tripId) as isActiveForCurrentTrip from contact LEFT JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE LEFT JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = $STATUS_ACTIVE GROUP BY contact.uid")
    fun getAllContactsWithIsUsedForTrip(tripId: Int): Single<List<Contact>>

    @Query("SELECT contact.* from contact LEFT JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE LEFT JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = $STATUS_ACTIVE AND trip.uid = :tripId")
    fun getAllActiveContactsForTrip(tripId: Int): Single<List<Contact>>

    @Query("select * from contact WHERE Contact.uid NOT IN ( " +
            "select contact.uid from Contact " +
            "LEFT JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE " +
            "LEFT JOIN trip ON contacttotriprelation.tripId = trip.uid " +
            "WHERE contact.status = $STATUS_ACTIVE AND trip.uid = :tripId) AND " +
            "contact.status = $STATUS_ACTIVE")
    fun getAllNotActiveContactsForTrip(tripId: Int): Single<List<Contact>>

    @Query("SELECT contact.*, Trip.isCurrent as isActiveForCurrentTrip from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = $STATUS_ACTIVE AND trip.isCurrent = 1 AND trip.status = $STATUS_ACTIVE")
    fun getActiveContactsForCurrentTrip(): LiveData<List<Contact>>

    @Query("SELECT contact.*, Trip.isCurrent as isActiveForCurrentTrip from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = $STATUS_ACTIVE AND trip.isCurrent = 1")
    fun getActiveContactsForCurrentTripRx(): Maybe<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY uid DESC LIMIT 1")
    fun getLastAddedContact(): Maybe<Contact>

    @Query("UPDATE contacttotriprelation SET status = $STATUS_DELETED WHERE tripId = :tripId")
    fun unbindAllContactsFromTrip(tripId: Int): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun bindContactsToTrip(vararg contactToTripRelation: ContactToTripRelation): Completable
}