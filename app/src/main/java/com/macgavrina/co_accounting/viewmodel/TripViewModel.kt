package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.ContactRepository
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.repositories.TripRepository
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Trip
import com.macgavrina.co_accounting.support.DateFormatter
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TripViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val snackbarMessage = SingleLiveEvent<String>()

    private var currentTrip = MutableLiveData<Trip>()
    private var currencies = MutableLiveData<List<Currency>>()
    private var contacts = MutableLiveData<List<Contact>>()
    private var selectedContacts = MutableLiveData<List<Contact>>()
    private var notSelectedContacts = MutableLiveData<List<Contact>>()
    private var allTripsAmount: Int = 0

    init {
        compositeDisposable.add(TripRepository().getActiveTripsAmount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({tripsAmount ->
                    allTripsAmount = tripsAmount
                }, {error ->
                    Log.d("Error getting trips amount")
                })
        )
    }

    fun tripIdIsReceivedFromActivity(tripId: Int) {
        Log.d("TripId is received from activity, = $tripId")
        compositeDisposable.add(TripRepository().getTripByIdRx(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({  trip ->
                    currentTrip.value = trip
                }, {error ->
                    Log.d("Error getting trip by id = $tripId, error = $error")
                }, {
                    Log.d("No trip with such id in DB, use draft")
                    getTripDraftAsCurrentTrip()
                })
        )

        compositeDisposable.add(CurrencyRepository().getAllActiveCurrenciesForTripRx(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ currenciesList ->
                    Log.d("Currencies active for trip is received from DB, size = ${currenciesList.size}, value = ${currenciesList}")
                    currencies.value = currenciesList
                }, { error ->
                    Log.d("Error getting currencies list for trip from DB, $error")
                })
        )

        compositeDisposable.add(ContactRepository().getAllContactsWithIsUsedForTrip(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ contactsList ->
                    contacts.value = contactsList
                }, { error ->
                    Log.d("Error getting contacts list from DB, $error")
                })
        )

        compositeDisposable.add(ContactRepository().getAllActiveContactsForTrip(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ contactsList ->
                    selectedContacts.value = contactsList
                }, { error ->
                    Log.d("Error getting active for trip contacts list from DB, $error")
                })
        )

        compositeDisposable.add(ContactRepository().getAllNotActiveContactsForTrip(tripId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ contactsList ->
                    notSelectedContacts.value = contactsList
                }, { error ->
                    Log.d("Error getting not-active for trip contacts list from DB, $error")
                })
        )
    }

    fun getAllContactsForCurrentTrip(): LiveData<List<Contact>> {
        return contacts
    }

    fun getSelectedContactsForCurrentTrip(): LiveData<List<Contact>> {
        return selectedContacts
    }

    fun getNotSelectedContactsForCurrentTrip(): LiveData<List<Contact>> {
        return notSelectedContacts
    }

    fun getCurrentTrip(): LiveData<Trip> {
        return currentTrip
    }

    fun getCurrencies(): LiveData<List<Currency>>? {
        return currencies
    }

    fun isTripCanBeDeleted(): Boolean {
        if (allTripsAmount > 1 ) return true
        return false
    }

    fun returnFromCurrenciesActivity() {
        Log.d("return from currencies activity")
        compositeDisposable.add(CurrencyRepository().getAllActiveCurrenciesForTripRx(currentTrip.value?.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ currenciesList ->
                    Log.d("Currencies active for trip is received from DB, size = ${currenciesList.size}, value = ${currenciesList}")
                    currencies.postValue(currenciesList)
                }, { error ->
                    Log.d("Error getting currencies list for trip from DB, $error")
                })
        )
    }

    fun deleteTrip() {
        if (currentTrip.value != null) {
            compositeDisposable.add(TripRepository().deleteTrip(currentTrip.value!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        Log.d("Trip is deleted")
                    }, {error ->
                        Log.d("Error updating trip, $error")
                    })
            )
        }
    }

    fun saveTrip() {

        if (currentTrip.value == null) return

        currentTrip.value?.status = STATUS_ACTIVE

        Log.d("Saving trip, newValue = ${currentTrip.value}")
        compositeDisposable.add(TripRepository().updateTrip(currentTrip.value!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Log.d("Trip is updated")
                }, {error ->
                    Log.d("Error updating trip, $error")
                })
        )

        recreateContactsForTripRelations(currentTrip.value!!.uid)
    }

    fun tripTitleIsChanged(newTitle: String) {
        currentTrip.value?.title = newTitle
    }

    fun startdateIsChanged(newValue: String) {
        currentTrip.value?.startdate = DateFormatter().getTimestampFromFormattedDate(newValue) ?: 0L
    }

    fun enddateIsChanged(newValue: String) {
        currentTrip.value?.enddate = DateFormatter().getTimestampFromFormattedDate(newValue) ?: 0L
    }

    fun onSelectedContactClick(contact: Contact) {
        Log.d("onSelectedContact click, contact = $contact")

        val selectedContactsListTemp = selectedContacts.value?.toMutableList()
        selectedContactsListTemp?.remove(contact)
        selectedContacts.value = selectedContactsListTemp

        val notSelectedContactsListTemp = notSelectedContacts.value?.toMutableList()
        notSelectedContactsListTemp?.add(contact)
        notSelectedContacts.value = notSelectedContactsListTemp
    }

    fun onNotSelectedContactClick(contact: Contact) {
        Log.d("onNotSelectedContact click, contact = $contact")

        val selectedContactsListTemp = selectedContacts.value?.toMutableList()
        selectedContactsListTemp?.add(contact)
        selectedContacts.value = selectedContactsListTemp

        val notSelectedContactsListTemp = notSelectedContacts.value?.toMutableList()
        notSelectedContactsListTemp?.remove(contact)
        notSelectedContacts.value = notSelectedContactsListTemp
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun createTripDraft() {
        compositeDisposable.add(TripRepository().createTripDraft()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    getTripDraftAsCurrentTrip()
                }, {
                    Log.d("Error creating trip draft")
                })
        )
    }

    private fun getTripDraftAsCurrentTrip() {
        Log.d("Getting trip draft as current trip...")
        compositeDisposable.add(
                TripRepository().getTripDraftRx()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({trip ->
                            Log.d("Trip draft is received from DB: $trip")
                            currentTrip.value = trip
                        }, {error ->
                            Log.d("Error getting trip draft, $error")
                        }, {
                            Log.d("There is no trip draft in DB, so create a new one")
                            createTripDraft()
                        })
        )
    }

    private fun recreateContactsForTripRelations(tripId: Int) {

        compositeDisposable.add(
                ContactRepository().unbindAllContactsFromTrip(tripId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (selectedContacts.value != null) {
                                bindSelectedContactsToTrip(selectedContacts.value!!, tripId)
                            }
                        }, {error ->
                            Log.d("Error unbinding all contacts from trip, $error")
                        })
        )
    }

    private fun bindSelectedContactsToTrip(contacts: List<Contact>, tripId: Int) {
        compositeDisposable.add(
                ContactRepository().bindContactsToTrip(contacts, tripId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("Contacts for trip are updated")
                        }, {error ->
                            Log.d("Error binding contacts to trip, $error")
                        })
        )
    }
}