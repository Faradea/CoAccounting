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

    private var repository: CurrencyRepository = CurrencyRepository()
    private var currentTripId: Int = -1

    init {
        subscribeToEventBus()
    }

    fun getAllCurrenciesForTrip(tripId: Int): LiveData<List<Currency>> {
        currentTripId = tripId
        return repository.getAllCurrenciesForTripLiveData(tripId)
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
                                repository.enableCurrencyForTrip(`object`.currencyId, currentTripId)
                            } else {
                                repository.disableCurrencyForTrip(`object`.currencyId, currentTripId)

                                repository.checkIfCurrencyIsUsedInTrip(`object`.currencyId, currentTripId)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({
                                            Log.d("Currency is used for trip $it times")
                                            if (it != 0) {
                                                repository.enableCurrencyForTrip(`object`.currencyId, currentTripId)
                                                toastMessage.value = "Currency is used for debts so it can't be deactivated for trip"
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