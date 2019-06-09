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

    private fun updateExpenseData(expenseId: Int, debtId: Int) {
        Log.d("updating expense data, expenseId = $expenseId, debtId = $debtId")
        val expense = Expense()
        expense.uid = expenseId
        expense.debtId = debtId
        expense.totalAmount = notSavedDebtSpentAmount.value.toString()
        expense.comment = ""
        compositeDisposable.add(
                expenseRepository.updateExpense(expense)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            Log.d("Expense is updated to $expense")
                            recreateReceiverWithAmount(expense)
                        }, {
                            Log.d("Error updating expense, $it")
                            toastMessage.postValue("DB error")
                        })
        )
    }

    private fun recreateReceiverWithAmount(expense: Expense) {
        Log.d("Recreating receiversWithAmount for expense = $expense")

        compositeDisposable.add(
                expenseRepository.deleteAllReceiverWithAmountForExpense(expense.uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            Log.d("All receiver with amount are deleted for expense")
                            addReceiversWithAmountForExpense(expense)
                        }, {
                            Log.d("Error deleting receivers with amount for expense, $it")
                            toastMessage.postValue("DB error")
                        })
        )

    }

    private fun addReceiversWithAmountForExpense(expense: Expense) {
        Log.d("Adding receivers with amount for expense, $expense, notSavedSelectedContactList size = ${notSavedSelectedContactList.value?.size}")

        val receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

        notSavedSelectedContactList.value?.forEach { contact ->
            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.expenseId = expense.uid.toString()
            receiverWithAmount.debtId = expense.debtId.toString()
            receiverWithAmount.contactId = contact.uid.toString()
            receiverWithAmount.amount = (notSavedDebtSpentAmount.value.toString().toDouble() / receiversWithAmountList.size).toString()
            receiversWithAmountList.add(receiverWithAmount)
        }

        compositeDisposable.add(
                expenseRepository.addReceiversWithAmountList(receiversWithAmountList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            Log.d("Receivers list is added, total was ${receiversWithAmountList.size}")
                        }, {
                            Log.d("Error adding receivers list, $it")
                        })
        )
    }
}