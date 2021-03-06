package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.ContactRepository
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ContactsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val snackbarMessage = SingleLiveEvent<String>()

    private var repository: ContactRepository = ContactRepository()
    private var allContactsForCurrentTrip: LiveData<List<Contact>> = repository.getAllContactsForCurrentTrip()

    private var lastDeletedContact: Contact? = null

    init {
    }

    fun getAllContactsForCurrentTrip(): LiveData<List<Contact>> {
        return allContactsForCurrentTrip
    }

    fun getContactById(contactId: String): LiveData<Contact> {
        return repository.getContactById(contactId)
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
    }

    fun insertContact(contact: Contact) {
        val subscription = repository.insertContact(contact)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    Log.d("Contact is inserted")
                    getLastAddedContactAndActivateItForTrip()
                }, {error ->
                    Log.d("Error inserting contact, $error")
                    toastMessage.value = "Database error"
                })
        compositeDisposable.add(subscription)
    }

    fun updateContact(contact: Contact) {
        val subscription = repository.updateContact(contact)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    Log.d("Contact is updated")
                }, {error ->
                    Log.d("Error updating contact, $error")
                    toastMessage.value = "Database error"
                })
        compositeDisposable.add(subscription)
    }

    fun addContactButtonIsPressed() {
        MainApplication.bus.send(Events.AddContact())
    }

    private fun getLastAddedContactAndActivateItForTrip() {
        repository.setLastAddedContactCheckedForCurrentTrip()
    }

    fun restoreLastDeletedContact() {
//
//        if (lastDeletedTrip == null) return
//
//        repository.restoreDeletedTrip(lastDeletedTrip!!)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
//                    override fun onSubscribe(d: Disposable) {}
//
//                    override fun onComplete() {
//                        Log.d("Trip is restored")
//                        if (lastDeletedTrip != null && lastDeletedTrip!!.isCurrent) {
//                            updateClickedTripIsCurrentField(lastDeletedTrip!!.uid.toString(), lastDeletedTrip!!.isCurrent)
//                        }
//                        lastDeletedTrip = null
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.d("Error restoring trip, $e")
//                        toastMessage.value = "Database error"
//                    }
//                })
    }

    fun safeDeleteContact(contact: Contact) {
        compositeDisposable.add(
                MainApplication.db.debtDAO().checkDebtsForContact(contact.uid.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ usedInDebtCount ->
                    if (usedInDebtCount == 0) {
                        Log.d("Contact isn't used as sender in debts, checking if he is present in expenses...")
                        checkIfContactIsUsedForExpenses(contact)
                    } else {
                        toastMessage.value = "Contact can't be deleted until it presents in debts"
                    }
                }, { error ->
                    Log.d("Error getting debts count for contact, $error")
                })
        )
    }

    private fun checkIfContactIsUsedForExpenses(contact: Contact) {
        MainApplication.db.receiverWithAmountForDBDAO().checkReceiverWithAmountForContact(contact.uid.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(count: Int) {
                        if (count == 0) {
                            Log.d("Contacts hasn't been used in debts and expenses, delete ir...")
                            deleteContact(contact)
                        } else {
                            toastMessage.value = "Contact can't be deleted until it presents in debts"
                            //getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting receivers with amount from DB, $e")
                    }

                })
    }

    private fun deleteContact(contact: Contact) {
        lastDeletedContact = contact
        snackbarMessage.value = "Contact is deleted"
        val subscription = repository.deleteContact(contact)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    Log.d("Contact is deleted")
                }, {error ->
                    Log.d("Error deleting contact, $error")
                    toastMessage.value = "Database error"
                })
        compositeDisposable.add(subscription)
    }
}