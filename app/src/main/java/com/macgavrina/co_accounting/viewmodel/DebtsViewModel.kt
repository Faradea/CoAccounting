package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.*
import com.macgavrina.co_accounting.room.*
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

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

    fun getAllActiveContactsForCurrentTrip(): LiveData<List<Contact>> {
        return contactsRepository.getAllActiveContactsForCurrentTrip()
    }

    fun deleteDebt(debt: Debt) {
        repository.deleteDebt(debt)
        //ToDo не забыть удалить expenses
    }

    fun updateDebtInDB(debt: Debt) {
        debt.currencyId = currentCurrencyId
        repository.updateDebtInDB(debt)
    }

    fun clearDebtDraft() {
        //ToDo не забыть удалить expenses (именно удалить а не проставить deleted)
        if (currentDebt != null && currentDebt!!.value != null) {
            currentDebt!!.value!!.datetime = System.currentTimeMillis().toString()
            currentDebt!!.value!!.spentAmount = ""
            currentDebt!!.value!!.comment = ""
            currentDebt!!.value!!.senderId = ""
        }
    }

    fun getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip(): LiveData<List<Currency>> {
        return CurrencyRepository().getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip()
    }

    private fun subscribeToEventBus() {

        val subscriptionToBus = MainApplication
                .bus
                .toObservable()
                .subscribe { `object` ->
                    when (`object`) {
                        is Events.OnClickExpenseItemList -> {
                            Log.d("Catch OnClickExpenseItemList event, debtId = ${`object`.myDebtId}, expenseId = ${`object`.myExpenseId}")
                            //displayExpenseActivity(`object`.myDebtId, `object`.myExpenseId)
                        }
                        is Events.OnClickCurrencyInDebt -> {
                            Log.d("Catch Events.OnClickCurrencyInDebt event, currencyId = ${`object`.currencyId}")
                            currentCurrencyId = `object`.currencyId
                            tripRepository.setupLastUsedCurrencyForCurrentTrip(`object`.currencyId)
                        }
                    }
                }

        compositeDisposable.add(subscriptionToBus)
    }

    //ToDo Перенести в DebtActivity
//    override fun displayExpenseActivity(debtId: Int, expenseId: Int?) {
//
//        val intent = Intent()
//        intent.action = "com.macgavrina.indebt.EXPENSE"
//        intent.putExtra("debtId", debtId)
//        if (expenseId != null) {
//            intent.putExtra("expenseId", expenseId)
//        } else {
//            intent.putExtra("expenseId", -1)
//        }
//
//        startActivity(intent)
//    }
}