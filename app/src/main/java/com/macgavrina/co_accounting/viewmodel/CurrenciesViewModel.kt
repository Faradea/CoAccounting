package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CurrenciesViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val currencyCantBeDisabledForTrip = SingleLiveEvent<Int>()

    private var repository: CurrencyRepository = CurrencyRepository()
    private var currentTripId: Int = -1
    private var currenciesForTrip: LiveData<List<Currency>>? = null

    init {
        subscribeToEventBus()
    }

    fun tripIdIsReceivedFromMainActivity(tripId: Int) {
        currentTripId = tripId
        currenciesForTrip = repository.getAllCurrenciesForTripLiveData(tripId)
    }

    fun getAllCurrenciesForTrip(): LiveData<List<Currency>>? {
        return currenciesForTrip
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
    }

    private fun subscribeToEventBus() {

        val subscriptionToBus = MainApplication
                .bus
                .toObservable()
                .subscribe { `object` ->
                    when (`object`) {
//                        is Events.DeletedContactIsRestored -> {
//                            Log.d("catch Events.DeletedContactIsRestored event, updating contacts list...")
//                            getAndDisplayAllContacts()
//                        }
                        is Events.OnClickCheckboxCurrency -> {
                            Log.d("catch Events.OnClickCheckboxCurrency event, isChecked = ${`object`.isChecked}, currencyId = ${`object`.currencyId}, tripId = $currentTripId")
                            if (`object`.isChecked) {
                                if (currenciesForTrip?.value?.find { currency ->
                                            currency.uid == `object`.currencyId && currency.activeTripId != 0
                                        } == null) {
                                    repository.enableCurrencyForTrip(`object`.currencyId, currentTripId)
                                } else {
                                    Log.d("Currency is already in the list, can be added twice")
                                }
                            } else {
                                repository.disableCurrencyForTrip(`object`.currencyId, currentTripId)

                                repository.checkIfCurrencyIsUsedInTrip(`object`.currencyId, currentTripId)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({
                                            Log.d("Currency is used for trip $it times")
                                            if (it != 0) {
                                                //currencyCantBeDisabledForTrip.value = `object`.currencyId
                                                toastMessage.value = "Currency is used for debts so it can't be deactivated for trip"
                                                repository.enableCurrencyForTrip(`object`.currencyId, currentTripId)
                                            }
                                        }, { error ->
                                            Log.d("Error checking of currency is used in trip, $error")
                                        })


                            }
                        }
                    }
                }

        compositeDisposable.add(subscriptionToBus)
    }
}