package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.repositories.CurrencyRepository
import com.macgavrina.co_accounting.room.Currency
import io.reactivex.disposables.CompositeDisposable

class CurrenciesViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()

    private var repository: CurrencyRepository = CurrencyRepository()

    init {
        subscribeToEventBus()
    }

    fun getAllCurrenciesForTrip(tripId: Int): LiveData<List<Currency>> {
        return repository.getAllCurrenciesForTrip(tripId)
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
                        }
                    }

        compositeDisposable.add(subscriptionToBus)
    }
}