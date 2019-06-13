package com.macgavrina.co_accounting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.repositories.*
import com.macgavrina.co_accounting.room.*
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
    private var expensesList: LiveData<List<Expense>>? = null
    private var debtDate: String = ""
    private var debtTime: String = ""

    private var expenseForSimpleMode: MutableLiveData<Expense> = MutableLiveData()
    private var selectedContactsForSimpleExpense: MutableLiveData<List<Contact>> = MutableLiveData()
    private var notSelectedContactsForSimpleExpense: MutableLiveData<List<Contact>> = MutableLiveData()
    private var debtSpentAmountForSimpleExpense: MutableLiveData<Double> = MutableLiveData()

    init {
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun getCurrentDebt(): MutableLiveData<Debt> {
        return currentDebt
    }

    fun getExpensesList(): LiveData<List<Expense>>? {
        return expensesList
    }

    fun getSelectedContactsForExpense(): MutableLiveData<List<Contact>> {
        return selectedContactsForSimpleExpense
    }

    fun getNotSelectedContactsForExpense(): MutableLiveData<List<Contact>> {
        return notSelectedContactsForSimpleExpense
    }

    fun getDebtSpentAmountForSimpleExpense(): MutableLiveData<Double> {
        return debtSpentAmountForSimpleExpense
    }

    fun debtIdIsReceivedFromIntent(debtId: Int) {

        val subscription = debtRepository.getDebtByIdRx(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ debt ->
                    Log.d("Debt data is received from DB, debt = $debt")
                    expensesList = expenseRepository.getAllExpensesForDebt(debt.uid)
                    currentDebt.value = debt
                }, { error ->
                    snackbarMessage.value = "Database error"
                    Log.d("Error getting debt data from server, error = $error")
                })
        compositeDisposable.add(subscription)
    }

    fun expenseIdForSimpleModeIsReceivedFromIntent(expenseId: Int) {
        val subscription = expenseRepository.getExpenseByIdRx(expenseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ expense ->
                    Log.d("Expense data is received from DB, debt = $expense")
                    initSelectedContactsForExpense(expense.uid)
                    initNotSelectedContactsForExpense(expense.uid)
                    expenseForSimpleMode.value = expense
                }, { error ->
                    snackbarMessage.value = "Database error"
                    Log.d("Error getting debt data from server, error = $error")
                }, {
                    Log.d("No expense with such id in DB")
                    initSelectedContactsForExpense(-1)
                    initNotSelectedContactsForExpense(-1)
                })
        compositeDisposable.add(subscription)
    }

    fun selectedContactIsClicked(selectedContact: Contact) {
        val selectedContactsListTemp = selectedContactsForSimpleExpense.value?.toMutableList()
        selectedContactsListTemp?.remove(selectedContact)
        selectedContactsForSimpleExpense.value = selectedContactsListTemp

        val notSelectedContactsListTemp = notSelectedContactsForSimpleExpense.value?.toMutableList()
        notSelectedContactsListTemp?.add(selectedContact)
        notSelectedContactsForSimpleExpense.value = notSelectedContactsListTemp

        //setAmountPerPersonForDebtTotal(debtTotalAmount)
    }

    fun notSelectedContactIsClicked(selectedContact: Contact) {
        val selectedContactsListTemp = selectedContactsForSimpleExpense.value?.toMutableList()
        selectedContactsListTemp?.add(selectedContact)
        selectedContactsForSimpleExpense.value = selectedContactsListTemp

        val notSelectedContactsListTemp = notSelectedContactsForSimpleExpense.value?.toMutableList()
        notSelectedContactsListTemp?.remove(selectedContact)
        notSelectedContactsForSimpleExpense.value = notSelectedContactsListTemp

        //setAmountPerPersonForDebtTotal(debtTotalAmount)
    }

    fun expenseIdForSimpleModeIsNotReceivedFromIntent() {
        initSelectedContactsForExpense(-1)
        initNotSelectedContactsForExpense(-1)
    }

    fun debtIdIsNotReceivedFromIntent() {
        val subscription = debtRepository.getDebtDraftRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ debt ->
                    Log.d("Debt draft is received from DB, debt = $debt")
                    expensesList = expenseRepository.getAllExpensesForDebt(debt.uid)
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
        if (currentDebt.value == null) return

        val tempDebt = currentDebt.value!!
        tempDebt.datetime = System.currentTimeMillis().toString()
        tempDebt.spentAmount = 0.0
        tempDebt.comment = ""
        tempDebt.senderId = -1

        currentDebt.value = tempDebt

        compositeDisposable.add(expenseRepository.deleteAllExpensesForDebt(currentDebt.value!!.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Log.d("All expenses for debt draft are deleted")
                }, {error ->
                    Log.d("Delete all expenses for debt error, $error")
                })
        )
    }

    fun debtSpentAmountIsChanged(newValue: Double) {
        currentDebt.value?.spentAmount = newValue
        debtSpentAmountForSimpleExpense.value = newValue
    }

    fun senderIdIsChanged(newValue: Int) {
        Log.d("SenderId is changed, new value = $newValue")
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

    fun timeIsChanged(newTime: String) {
        debtTime = newTime
        Log.d("time is changed, newValue = $newTime")
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
        saveCurrentDateWithStatus("active")
    }

    fun onBackPressed() {
        Log.d("Saving draft, ${currentDebt.value}")
        if (currentDebt.value?.status != "active") {
            saveCurrentDateWithStatus("draft")
        }
    }

    private fun saveCurrentDateWithStatus(debtStatus: String) {
        if (currentDebt.value != null) {
            if (currentDebt.value!!.currencyId == -1) {
                defineDefaultCurrencyForTrip()
            }
            currentDebt.value?.status = debtStatus
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
                Log.d("formattedDateTime = $debtDate $debtTime, timestamp = $formattedDateTime")
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
        Log.d("Saving expense for simple mode, expense = ${expenseForSimpleMode.value}, selectedContactsList size = ${selectedContactsForSimpleExpense.value?.size}")

        if (expenseForSimpleMode.value == null || expenseForSimpleMode.value!!.uid  == -1) {

            if (selectedContactsForSimpleExpense.value == null) return
            if (selectedContactsForSimpleExpense.value!!.isEmpty()) return

            val expense = Expense()
            compositeDisposable.add(
                    expenseRepository.insertNewExpense(expense)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe ({
                                Log.d("New expense is created")
                                expenseRepository.getLastAddedExpenseId()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe ({
                                            Log.d("Last added expenseId = $it")
                                            if (currentDebt.value?.uid != null) {
                                                updateExpenseData(it, currentDebt.value!!.uid)
                                            }
                                        }, {
                                            Log.d("Error getting last expenseId from DB, $it")
                                            toastMessage.postValue("DB error")
                                        })
                            }, {
                                Log.d("Error inserting new expense to DB, $it")
                                toastMessage.postValue("Error creating new expense, DB error")
                            })
            )

        } else {
            if (selectedContactsForSimpleExpense.value == null) {
                deleteExpenseForSimpleMode()
                return
            }

            if (selectedContactsForSimpleExpense.value!!.isEmpty()) {
                deleteExpenseForSimpleMode()
                return
            }

            if (expenseForSimpleMode.value?.uid != null && currentDebt.value?.uid != null) {
                updateExpenseData(expenseForSimpleMode.value!!.uid, currentDebt.value!!.uid)
            }
        }
    }

    private fun deleteExpenseForSimpleMode() {
        compositeDisposable.add(expenseRepository.deleteAllExpensesForDebt(currentDebt.value!!.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Log.d("All expenses are deleted for debt")
                }, {
                    Log.d("Error deleting expenses for debt, $it")
                    toastMessage.postValue("DB error")
                })
        )
    }

    private fun updateExpenseData(expenseId: Int, debtId: Int) {
        Log.d("updating expense data, expenseId = $expenseId, debtId = $debtId")
        val expense = Expense()
        expense.uid = expenseId
        expense.debtId = debtId
        expense.totalAmount = currentDebt.value?.spentAmount ?: 0.0
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
        Log.d("Adding receivers with amount for expense, $expense, selectedContactList size = ${selectedContactsForSimpleExpense.value?.size}")

        val receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

        selectedContactsForSimpleExpense.value?.forEach { contact ->
            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.expenseId = expense.uid.toString()
            receiverWithAmount.debtId = expense.debtId.toString()
            receiverWithAmount.contactId = contact.uid.toString()
            receiverWithAmount.amount = ((currentDebt.value?.spentAmount ?: 0.0) / receiversWithAmountList.size).toString()
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

    private fun initSelectedContactsForExpense(expenseId: Int) {
        val subscription = expenseRepository.getSelectedContactsForExpenseRx(expenseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ selectedContactsList ->
                    Log.d("Selected contacts list is received from DB")
                    selectedContactsForSimpleExpense.value = selectedContactsList
                }, {error ->
                    Log.d("Error getting selected contact list from DB, error = $error")
                })
        compositeDisposable.add(subscription)
    }

    private fun initNotSelectedContactsForExpense(expenseId: Int) {
        val subscription = expenseRepository.getNotSelectedContactsForExpenseRx(expenseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ notSelectedContactsList ->
                    Log.d("Not selected contacts list is received from DB")
                    notSelectedContactsForSimpleExpense.value = notSelectedContactsList
                }, {error ->
                    Log.d("Error getting not selected contact list from DB, error = $error")
                })
        compositeDisposable.add(subscription)
    }
}