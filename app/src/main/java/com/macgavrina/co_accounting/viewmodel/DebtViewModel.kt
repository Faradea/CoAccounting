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
    private var contactsListForCurrentTrip = contactsRepository.getAllActiveContactsForCurrentTrip()
    private var contactsListForAllTripsCount = contactsRepository.getAllContactsCount()
    private var expensesListForSimpleMode: LiveData<List<Expense>>? = null
    private var expensesListForExpertMode: LiveData<List<Expense>>? = null
    private var debtDate: String = ""
    private var debtTime: String = ""
    private var currentTrip = TripRepository().getCurrentTripLiveData()
    private var senderForCurrentTrip = MutableLiveData<Contact>()
    private var expensesSum: LiveData<Double>? = null

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

    fun getCurrentTrip(): LiveData<Trip> {
        return currentTrip
    }

    fun getCurrentDebt(): MutableLiveData<Debt> {
        return currentDebt
    }

    fun getExpensesListForSimpleMode(): LiveData<List<Expense>>? {
        return expensesListForSimpleMode
    }

    fun getExpensesListForExpertMode(): LiveData<List<Expense>>? {
        return expensesListForExpertMode
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

    fun getSenderForCurrentTrip(): MutableLiveData<Contact> {
        return senderForCurrentTrip
    }

    fun getExpensesSum(): LiveData<Double>? {
        return expensesSum
    }

    fun debtIdIsReceivedFromIntent(debtId: Int) {

        val subscription = debtRepository.getDebtByIdRx(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ debt ->
                    Log.d("Debt data is received from DB, debt = $debt")
                    expensesListForSimpleMode = expenseRepository.getAllExpensesForDebtSimpleMode(debtId)
                    expensesListForExpertMode = expenseRepository.getAllExpensesForDebtExpertMode(debtId)
                    currentDebt.value = debt

                    initializeSender(debt.senderId)

                    expensesSum = expenseRepository.getExpensesSumForDebtAndExpertMode(debtId)

                }, { error ->
                    snackbarMessage.value = "Database error"
                    Log.d("Error getting debt data from server, error = $error")
                })
        compositeDisposable.add(subscription)
    }

    fun expenseIdForSimpleModeIsReceivedFromIntent(expenseId: Int) {
        Log.d("expenseIdForSimpleModeIsReceivedFromIntent, $expenseId")
        val subscription = expenseRepository.getExpenseByIdRx(expenseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ expense ->
                    Log.d("Expense data is received from DB, debt = $expense")
                    if (selectedContactsForSimpleExpense.value == null && notSelectedContactsForSimpleExpense.value == null) {
                        initSelectedContactsForExpense(expense.uid)
                        initNotSelectedContactsForExpense(expense.uid)
                        expenseForSimpleMode.value = expense
                    }
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
        Log.d("expenseIdForSimpleModeIsNotReceivedFromIntent")
        if (selectedContactsForSimpleExpense.value == null && notSelectedContactsForSimpleExpense.value == null) {
            initSelectedContactsForExpense(-1)
            initNotSelectedContactsForExpense(-1)
        }
    }

    fun debtIdIsNotReceivedFromIntent() {
        val subscription = debtRepository.getDebtDraftRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ debt ->
                    Log.d("Debt draft is received from DB, debt = $debt")
                    expensesListForSimpleMode = expenseRepository.getAllExpensesForDebtSimpleMode(debt.uid)
                    expensesListForExpertMode = expenseRepository.getAllExpensesForDebtExpertMode(debt.uid)

                    expensesSum = expenseRepository.getExpensesSumForDebtAndExpertMode(debt.uid)
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


    private fun initializeSender(senderId: Int) {
        if (senderId != -1) {
            compositeDisposable.add(
                    contactsRepository.getContactByIdRx(senderId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe ({ sender ->
                                senderForCurrentTrip.value = sender
                            }, {error ->
                                Log.d("Error getting contact data for debt sender, error = $error")
                            })
            )
        }
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
        return contactsListForCurrentTrip
    }

    fun getAllContactsCount(): LiveData<Int> {
        return contactsListForAllTripsCount
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
        tempDebt.datetime = System.currentTimeMillis()
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
        saveCurrentDebtWithStatus("active")
    }

    fun onBackPressed() {
        Log.d("Saving draft, ${currentDebt.value}")
        if (currentDebt.value?.status != "active") {
            saveCurrentDebtWithStatus("draft")
        }
    }

    private fun saveCurrentDebtWithStatus(debtStatus: String) {

        Log.d("Saving current debt (${currentDebt.value} with status = $debtStatus")
        if (currentDebt.value != null) {
            if (currentDebt.value!!.currencyId == -1) {
                defineDefaultCurrencyForTrip()
            }
            currentDebt.value?.status = debtStatus
            debtRepository.updateDebtInDB(currentDebt.value!!)

            val senderWithAmount = SenderWithAmount()
            senderWithAmount.amount = currentDebt.value!!.spentAmount
            senderWithAmount.contactId = currentDebt.value!!.senderId
            senderWithAmount.debtId = currentDebt.value!!.uid

            deleteAllSendersWithAmountForDebtAndAddNewOne(currentDebt.value!!.uid, senderWithAmount)

            if (!currentDebt.value!!.expertModeIsEnabled) {
                deleteAllExpensesAndSaveNewOneForSimpleMode()
            } else {
                deleteAllExpensesForSimpleMode(currentDebt.value!!.uid)
            }
        }
    }

    private fun deleteAllSendersWithAmountForDebtAndAddNewOne(debtId: Int, senderWithAmount: SenderWithAmount) {
        compositeDisposable.add(
                debtRepository.deleteAllSendersWithAmountForDebt(debtId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("All senders with amount for debt are deleted")
                            saveSenderWithAmountForDebt(senderWithAmount)
                        }, {error ->
                            Log.d("Error deleting senders with amount for debt, $error")
                        })
        )
    }

    private fun saveSenderWithAmountForDebt(senderWithAmount: SenderWithAmount) {
        Log.d("Adding senderWithAmount for debt, $senderWithAmount")
        compositeDisposable.add(
                debtRepository.addSenderWithAmountForDebt(senderWithAmount)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("SenderWithAmount ($senderWithAmount) is saved for debt")
                        }, {error ->
                            Log.d("Error adding sender with amount for debt, $error")
                        })
        )
    }

    private fun deleteAllExpensesForSimpleMode(debtId: Int) {
        compositeDisposable.add(
                expenseRepository.deleteAllExpensesForDebtAndSimpleMode(debtId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("All expenses for simple mode are deleted")
                            debtRepository.checkDebtCorrectness(currentDebt.value!!)
                        }, {error ->
                            Log.d("Error deleting expenses for simple mode, $error")
                        })
        )
    }
    private fun dateTimeIsChanged() {
        if (debtDate.isNotEmpty()) {

            if (debtTime.isEmpty()) {
                val formattedDate = DateFormatter().getTimestampFromFormattedDate(debtDate)
                if (formattedDate != null) {
                    currentDebt.value?.datetime = formattedDate
                }
            } else {
                val formattedDateTime = DateFormatter().getTimestampFromFormattedDateTime(
                        "$debtDate $debtTime")
                Log.d("formattedDateTime = $debtDate $debtTime, timestamp = $formattedDateTime")
                if (formattedDateTime != null) {
                    currentDebt.value?.datetime = formattedDateTime
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

    private fun deleteAllExpensesAndSaveNewOneForSimpleMode() {

        if (currentDebt.value == null) return
        compositeDisposable.add(
                expenseRepository.deleteAllExpensesForDebt(currentDebt.value!!.uid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.d("All expenses are deleted from debt")
                            expenseForSimpleMode.value = null
                            saveExpenseFromSimpleMode()
                        }, { error ->
                            Log.d("Error deleting expenses for debt before saving simple mode, $error")
                        })
        )
    }

    private fun saveExpenseFromSimpleMode() {
        Log.d("Saving expense for simple mode, expense = ${expenseForSimpleMode.value}, selectedContactsList size = ${selectedContactsForSimpleExpense.value?.size}")

        if (expenseForSimpleMode.value == null || expenseForSimpleMode.value!!.uid  == -1) {

            if (selectedContactsForSimpleExpense.value == null) {
                if (currentDebt.value != null) {
                    debtRepository.checkDebtCorrectness(currentDebt.value!!)
                }
                return
            }
            if (selectedContactsForSimpleExpense.value!!.isEmpty()) {
                if (currentDebt.value != null) {
                    debtRepository.checkDebtCorrectness(currentDebt.value!!)
                }
                return
            }

            val expense = Expense()
            expense.debtId = currentDebt.value?.uid ?: -1
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
                    Log.d("All expenses are deleted for debt, selectedContactsForSimpleExpense.value?.isEmpty() = ${selectedContactsForSimpleExpense.value?.isEmpty()}")
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
        Log.d("Adding receivers with amount for expenseId = ${expense.uid}, expense = $expense, selectedContactList size = ${selectedContactsForSimpleExpense.value?.size}")

        val receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

        if (selectedContactsForSimpleExpense.value == null) return

        selectedContactsForSimpleExpense.value?.forEach { contact ->
            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.expenseId = expense.uid
            receiverWithAmount.debtId = expense.debtId
            receiverWithAmount.contactId = contact.uid
            receiverWithAmount.amount = ((currentDebt.value?.spentAmount ?: 0.0) / selectedContactsForSimpleExpense.value!!.size)
            receiversWithAmountList.add(receiverWithAmount)
            Log.d("receiverWithAmount: $receiverWithAmount")
        }



        compositeDisposable.add(
                expenseRepository.addReceiversWithAmountList(receiversWithAmountList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            Log.d("Receivers list is added, total was ${receiversWithAmountList.size}")
                            debtRepository.checkDebtCorrectness(currentDebt.value!!)
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