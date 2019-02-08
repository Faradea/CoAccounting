package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.ContactToTripRelation
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class ContactsPresenter: BasePresenter<ContactsContract.View>(), ContactsContract.Presenter {

    private var subscriptionToBus: Disposable? = null
    private var currentTrip: Trip? = null

    override fun attachView(baseViewContract: ContactsContract.View) {
        super.attachView(baseViewContract)
        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
    }

    override fun viewIsReady() {
        Log.d("Contacts fragment is ready")
        getAndDisplayAllContacts()
    }

    override fun addContactButtonIsPressed() {
        Log.d("Add new contact button is pressed")
        MainApplication.bus.send(Events.AddContact())
    }

    override fun contactItemIsSelected(selectedContactId: Int) {
        Log.d("User clicked on contact, selectedContactId = ${selectedContactId}")
    }

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.DeletedContactIsRestored -> {
                                Log.d("catch Events.DeletedContactIsRestored event, updating contacts list...")
                                getAndDisplayAllContacts()
                            }
                            is Events.OnClickCheckboxContactList -> {
                                Log.d("catch Events.OnClickCheckboxContactList event")
                                updateContactsListForTrip(`object`.contactId, `object`.isChecked)
                            }
                        }
                    }
        }
    }

    private fun unsubscribeFromEventBus() {
        if (subscriptionToBus != null) {
            subscriptionToBus?.dispose()
            subscriptionToBus = null
        }
    }

    private fun getAndDisplayAllContacts() {

        getView()?.showProgress()

        TripRepository(MainApplication.instance).getCurrentTrip()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({ maybeTrip ->
                    maybeTrip
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe ({ trip ->
                                currentTrip = trip
                                getAllContactsRelationForTrip(trip)
                            }, {error ->
                                Log.d("Error getting current trip from DB, $error")
                                getView()?.displayToast("Database error")
                            })
                }, {error ->
                    Log.d("Error getting current trip from DB, $error")
                    getView()?.displayToast("Database error")
                })
    }

    private fun getAllContactsRelationForTrip(trip: Trip) {

        MainApplication.db.contactToTripRelationDAO().getAllContactsForTrip(trip.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<ContactToTripRelation>>() {
                    override fun onSuccess(contactsActiveForTripList: List<ContactToTripRelation>) {
                        Log.d("Contacts active for trip list is received from DB, size= ${contactsActiveForTripList.size}")
                        contactsActiveForTripList.forEach { contactToTripRelation ->
                            Log.d("${contactToTripRelation.contactId} - ${contactToTripRelation.tripId}")
                        }
                        getAndDisplayAllContactsWithCurrentTripCheckbox(contactsActiveForTripList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting contacts active for trip from DB, $e")
                        getView()?.hideProgress()
                        getView()?.displayToast("Database error")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                        getAndDisplayAllContactsWithCurrentTripCheckbox(null)
                    }
                })
    }

    private fun getAndDisplayAllContactsWithCurrentTripCheckbox(contactsActiveForTrip: List<ContactToTripRelation>?) {
        getView()?.showProgress()
        MainApplication.db.contactDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        Log.d("Contacts list is received from DB, size = ${contactsList.size}")
                        getView()?.hideProgress()
                        getView()?.initializeList(contactsList, contactsActiveForTrip)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error loading contacts from DB, $e")
                        getView()?.hideProgress()
                        getView()?.displayToast("Database error")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                    }
                })
    }

    private fun updateContactsListForTrip(contactId: String, checked: Boolean) {

        Log.d("updateContactsListForTrip, contactId = $contactId, checked = $checked, currentTrip = $currentTrip")

        if (currentTrip == null) return

        if (checked) {
            Completable.fromAction {
                MainApplication.db.contactToTripRelationDAO().addContactToTripRelation(ContactToTripRelation(contactId.toInt(), currentTrip!!.uid))
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {}

                        override fun onComplete() {
                            Log.d("Contact to trip relation is added")
                        }

                        override fun onError(e: Throwable) {
                            Log.d("Error adding contact to trip relation, $e")
                            getView()?.displayToast("Database error")
                        }
                    })
        } else {

            checkDebtsAndExpensesForContact(contactId)
        }
    }


    fun checkDebtsAndExpensesForContact(contactId: String) {

        MainApplication.db.debtDAO().checkDebtsForContactAndCurrentTrip(contactId, "active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onComplete() {
                    }

                    override fun onSuccess(list: List<Debt>) {
                        if (list.isEmpty()) {

                            Log.d("Contact isn't used as sender in debts, checking if he is present in expenses...")
                            MainApplication.db.receiverWithAmountForDBDAO().checkReceiverWithAmountForContactAndCurrentTrip(contactId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSubscribe(d: Disposable) {
                                        }

                                        override fun onSuccess(count: Int) {
                                            if (count == 0) {
                                                Log.d("Contacts hasn't been used in debts and expenses, delete ir...")
                                                disableContactForTrip(contactId)
                                            } else {
                                                getView()?.updateList()
                                                MainApplication.bus.send(Events.ContactCannotBeDisableForTrip())
                                            }
                                        }

                                        override fun onError(e: Throwable) {
                                            Log.d("Error getting receivers with amount from DB, $e")
                                        }

                                    })
                        } else {
                            getView()?.updateList()
                            MainApplication.bus.send(Events.ContactCannotBeDisableForTrip())
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting debts for contact from db, $e")
                    }
                })

    }

    fun disableContactForTrip(contactId: String) {

        Completable.fromAction {
            MainApplication.db.contactToTripRelationDAO().deleteContactToTripRelation(contactId.toInt(), currentTrip!!.uid)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Contact to trip relation is deleted")
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error deleting contact to trip relation, $e")
                        getView()?.displayToast("Database error")
                    }
                })
    }

}