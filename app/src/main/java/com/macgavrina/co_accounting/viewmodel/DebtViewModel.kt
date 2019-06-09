package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.*
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.support.DateFormatter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DebtViewModel(application: Application) : AndroidViewModel(MainApplication.instance) {

    internal val snackbarMessage = SingleLiveEvent<String>()
    internal val toastMessage = SingleLiveEvent<String>()

    private var debtRepository: DebtRepository = DebtRepository()
    private var expenseRepository = ExpenseRepository()
    private var contactsRepository = ContactRepository()
    private var tripRepository = TripRepository()

    private val compositeDisposable = CompositeDisposable()

    private var currentDebt: MutableLiveData<Debt> = MutableLiveData()
    private var currenciesList: LiveData<List<Currency>> = CurrencyRepository().getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip()
    private var contactsList = contactsRepository.getAllActiveContactsForCurrentTrip()
    private var expensesList: MutableLiveData<List<Expense>> = MutableLiveData()
    private var debtDate: String = ""
    private var debtTime: String = ""

    init {
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun getCurrentDebt(): MutableLiveData<Debt> {
        return currentDebt
    }

    fun debtIdIsReceivedFromIntent(debtId: Int) {

        val subscription = debtRepository.getDebtByIdRx(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ debt ->
                    Log.d("Debt data is received from DB, debt = $debt")
                    currentDebt.value = debt
                }, { error ->
                    snackbarMessage.value = "Database error"
                    Log.d("Error getting debt data from server, error = $error")
                })
        compositeDisposable.add(subscription)
    }

    fun debtIdIsNotReceivedFromIntent() {
        val subscription = debtRepository.getDebtDraftRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ debt ->
                    Log.d("Debt draft is received from DB, debt = $debt")
                    currentDebt.value = debt
                }, { error ->
                    snackbarMessage.value = "Database error"
                    Log.d("Error getting debt draft data from server, error = $error")
                }, {
                    Log.d("There is no debt draft in DB, creating a new one")
                    createDebtDraft()
                })
        compositeDisposable.add(subscription)
    }

    private fun createDebtDraft() {
        val subscription = debtRepository.createDebtDraft()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Log.d("Debt draft is created")
                    debtIdIsNotReceivedFromIntent()
                }, {error ->
                    Log.d("Error creating debt draft, error = $error")
                })
        compositeDisposable.add(subscription)
    }

    fun getAllActiveContactsForCurrentTrip(): LiveData<List<Contact>> {
        return contactsList
    }

    fun getAllActiveCurrenciesWithLastUsedMarkerForCurrentTrip(): LiveData<List<Currency>> {
        return currenciesList
    }

    fun deleteDebt() {
        if (currentDebt.value?.uid != null) {
            debtRepository.deleteDebt(currentDebt.value!!)
        }
        //ToDo не забыть удалить expenses
    }

    fun clearDebtDraft() {
        //ToDo не забыть удалить expenses (именно удалить а не проставить deleted)
            currentDebt.value?.datetime = System.currentTimeMillis().toString()
            currentDebt.value?.spentAmount = 0.0
            currentDebt.value?.comment = ""
            currentDebt.value?.senderId = -1
    }

    fun debtSpentAmountIsChanged(newValue: Double) {
        currentDebt.value?.spentAmount = newValue
    }

    fun senderIdIsChanged(newValue: Int) {
        currentDebt.value?.senderId = newValue
    }

    fun onCurrencyClick(currencyId: Int) {
        currentDebt.value?.currencyId = currencyId
        tripRepository.setupLastUsedCurrencyForCurrentTrip(currencyId)
    }

    fun dateIsChanged(newDate: String) {
        debtDate = newDate
        dateTimeIsChanged()
    }

    fun commentIsChanged(newValue: String) {
        currentDebt.value?.comment = newValue
    }

    fun expertModeSwitchStatusIsChanged(newValue: Boolean) {
        Log.d("expertMode switch is changed, newValue = $newValue")
        currentDebt.value?.expertModeIsEnabled = newValue
    }

    fun doneButton() {

        Log.d("Saving debt, ${currentDebt.value}")
        if (currentDebt.value != null) {
            if (currentDebt.value!!.currencyId == -1) {
                defineDefaultCurrencyForTrip()
            }
            currentDebt.value?.status = "active"
            debtRepository.updateDebtInDB(currentDebt.value!!)
        }

        if (currentDebt.value != null && !currentDebt.value!!.expertModeIsEnabled) {
            saveExpenseFromSimpleMode()
        }
    }

    private fun dateTimeIsChanged() {
        if (debtDate.isNotEmpty()) {

            if (debtTime.isEmpty()) {
                val formattedDate = DateFormatter().getTimestampFromFormattedDate(debtDate)
                if (formattedDate != null) {
                    currentDebt.value?.datetime = formattedDate.toString()
                }
            } else {
                val formattedDateTime = DateFormatter().getTimestampFromFormattedDateTime(
                        "$debtDate $debtTime")
                if (formattedDateTime != null) {
                    currentDebt.value?.datetime = formattedDateTime.toString()
                }
            }
        }
    }

    private fun defineDefaultCurrencyForTrip() {
        if ( currenciesList.value != null && currenciesList.value!!.isNotEmpty()) {
                currenciesList.value!!.forEach { currency ->
                    if (currency.lastUsedCurrencyId != -1) {
                        currentDebt.value?.currencyId = currency.lastUsedCurrencyId
                    } else {
                        currentDebt.value?.currencyId = currency.uid
                    }
                    return@forEach
            }
        }
    }

    private fun saveExpenseFromSimpleMode() {
//        Log.d("Saving expense for simple mode, expenseId = $expenseIdForSimpleMode, debtId = $debtId")
//
//        if (expenseIdForSimpleMode == -1) {
//            val expense = Expense()
//            compositeDisposable.add(
//                    expenseRepository.insertNewExpense(expense)
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe ({
//                                Log.d("New expense is created")
//                                expenseRepository.getLastAddedExpenseId()
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe ({
//                                            Log.d("Last added expenseId = $it")
//                                            updateExpenseData(it, debtId)
//                                        }, {
//                                            Log.d("Error getting last expenseId from DB, $it")
//                                            toastMessage.postValue("DB error")
//                                        })
//                            }, {
//                                Log.d("Error inserting new expense to DB, $it")
//                                toastMessage.postValue("Error creating new expense, DB error")
//                            })
//            )
//
//        } else {
//            updateExpenseData(expenseIdForSimpleMode, debtId)
//        }
    }
}