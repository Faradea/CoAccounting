package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.*
import com.macgavrina.co_accounting.room.*
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import kotlin.math.exp

const val EXPENSE_ID_KEY = "expenseId"

class DebtsViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    private val compositeDisposable = CompositeDisposable()
    internal val toastMessage = SingleLiveEvent<String>()
    internal val snackbarMessage = SingleLiveEvent<String>()

    private var repository: DebtRepository = DebtRepository()
    private var expenseRepository = ExpenseRepository()
    private var contactsRepository = ContactRepository()
    private var tripRepository = TripRepository()

    private var allDebtsForCurrentTrip: LiveData<List<Debt>> = repository.getAllDebtsForCurrentTrip()
    private var currentDebt: LiveData<Debt>? = null
    private var currentCurrencyId: Int = -1
    private var currenciesListForCurrentDebt: LiveData<List<Currency>>? = null

    val notSavedSelectedContactList = MutableLiveData<List<Contact>>()
    val notSavedNotSelectedContactList = MutableLiveData<List<Contact>>()
    val notSavedDebtSpentAmount = MutableLiveData<Double>()

    //private var lastDeletedContact: Contact? = null

    init {
        subscribeToEventBus()
    }

    fun getCurrentDebt(): Debt? {
        return currentDebt?.value
    }

    fun getAllDebtsForCurrentTrip(): LiveData<List<Debt>> {
        return allDebtsForCurrentTrip
    }

//    fun getDebtById(debtId: Int): LiveData<Debt> {
//        return repository.getContactById(contactId)
//    }

    fun addDebtButtonIsPressed() {
        MainApplication.bus.send(Events.AddDebt())
    }

    fun viewIsDestroyed() {
        compositeDisposable.clear()
    }

    fun getDebtById(debtId: Int): LiveData<Debt>? {

        if (debtId != -1) {
            currentDebt = repository.getDebtById(debtId)
        } else {
            currentDebt = repository.getDebtDraft()
        }

        return currentDebt
    }

    fun createDebtDraft(): Completable {
        return repository.createDebtDraft()
    }

    fun getAllExpensesForDebt(debtId: Int): LiveData<List<Expense>> {
        return expenseRepository.getAllExpensesForDebt(debtId)
    }

    fun getSelectedContactsForExpense(expenseId: Int): LiveData<List<Contact>> {
        return expenseRepository.getSelectedContactsForExpense(expenseId)
    }

    fun getNotSelectedContactsForExpense(expenseId: Int): LiveData<List<Contact>> {
        return expenseRepository.getNotSelectedContactsForExpense(expenseId)
    }

    fun onCurrencyClick(currencyId: Int) {
        currentCurrencyId = currencyId
        if (currentDebt != null && currentDebt?.value != null) {
            currentDebt!!.value!!.currencyId = currencyId
        }
        tripRepository.setupLastUsedCurrencyForCurrentTrip(currencyId)
    }

    private fun subscribeToEventBus() {

        val subscriptionToBus = MainApplication
                .bus
                .toObservable()
                .subscribe { `object` ->
                    when (`object`) {
                        is Events.OnClickCurrencyInDebt -> {
                            Log.d("Catch OnClickCurrencyInDebt event, currencyId = ${`object`.currencyId}")
                            onCurrencyClick(`object`.currencyId)
                        }
                    }
                }

        compositeDisposable.add(subscriptionToBus)
    }
}