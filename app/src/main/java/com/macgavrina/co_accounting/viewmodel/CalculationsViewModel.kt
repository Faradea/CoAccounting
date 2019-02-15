package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.repositories.CalculationsRepository
import com.macgavrina.co_accounting.room.Calculation
import io.reactivex.disposables.CompositeDisposable

class CalculationsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    //private val compositeDisposable = CompositeDisposable()
    private var repository: CalculationsRepository = CalculationsRepository()
    private var allCalculationsForCurrentTrip: LiveData<List<Calculation>>

    init {
        //subscribeToEventBus()
        allCalculationsForCurrentTrip = repository.getAllCalculationsForCurrentTrip()
    }

    fun getAllCalculationsForCurrentTrip(): LiveData<List<Calculation>> {
        return allCalculationsForCurrentTrip
    }

    fun viewIsDestroyed() {
        //compositeDisposable.clear()
    }

}