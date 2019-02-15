package com.macgavrina.co_accounting.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.ContactDAO
import com.macgavrina.co_accounting.room.ContactToTripRelation
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ContactRepository {

    private var contactDao: ContactDAO = MainApplication.db.contactDAO()

    private var allContactsForCurrentTrip: LiveData<List<Contact>>

    init {
        allContactsForCurrentTrip = contactDao.getContactsForCurrentTrip()
    }

    fun getAllContactsForCurrentTrip(): LiveData<List<Contact>> {
        Log.d("getAllContactsForCurrentTrip, allContactsForCurrentTrip.value.size = ${allContactsForCurrentTrip.value?.size}")
        return allContactsForCurrentTrip
    }

    fun getContactById(contactId: String): LiveData<Contact> {

        return contactDao.getContactByIds(contactId)
    }

    fun insertContact(contact: Contact): Completable {
        return Completable.fromAction {
            contactDao.insertContact(contact)
        }
    }

    fun updateContact(contact: Contact): Completable {
        return Completable.fromAction {
            contactDao.updateContact(contact)
        }
    }

    fun deleteContact(contact: Contact): Completable {
        return Completable.fromAction {
            contactDao.deleteContact(contact.uid)
        }
    }

    fun setIsContactCheckedForCurrentTrip(contactId: String, isChecked: Boolean) {

        TripRepository().getCurrentTrip()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({ maybeTrip ->
                    maybeTrip
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe ({ trip ->
                                if (isChecked) {
                                    Completable.fromAction {
                                        MainApplication.db.contactToTripRelationDAO().addContactToTripRelation(ContactToTripRelation(contactId.toInt(), trip.uid))
                                    }
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe({
                                                Log.d("Relation is established for contactId = $contactId and tripId = ${trip.uid}")
                                            }, {error ->
                                                Log.d("Error adding contact to trip relation, $error")
                                            })
                                } else {
                                    Completable.fromAction {
                                        MainApplication.db.contactToTripRelationDAO().deleteContactToTripRelation(contactId.toInt(), trip.uid)
                                    }
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe ({
                                                "Relation is deleted for contactId = $contactId and tripId = ${trip.uid}"
                                            } , { error ->
                                                Log.d("Error deleting contact to trip relation, $error")
                                            })
                                }
                            }, {error ->
                                Log.d("Error getting current trip from DB, $error")
                            })
                }, {error ->
                    Log.d("Error getting current trip from DB, $error")
                })
    }

//    fun getTripsAmount(): Observable<Int> {
//        return Observable.fromCallable { tripDao.getTripsCount() }
//    }
//
//    fun restoreDeletedTrip(trip: Trip): Completable {
//        trip.status = "active"
//        return Completable.fromAction {
//            tripDao.updateTrip(trip)
//        }
//    }

}