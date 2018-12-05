package com.macgavrina.co_accounting.presenters

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.DebtActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.DebtsProvider
import com.macgavrina.co_accounting.providers.ExpenseProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.support.DateFormatter
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.add_debt_fragment.*
import java.text.ParseException
import java.util.*

class DebtActivityPresenter:BasePresenter<DebtActivityContract.View>(), DebtActivityContract.Presenter, DebtsProvider.DatabaseCallback, ContactsProvider.DatabaseCallback, ExpenseProvider.DatabaseCallback {

    private var subscriptionToBus: Disposable? = null

    var senderId: Int? = null
    lateinit var debt: Debt
    lateinit var contactsIdToNameMap: Map<String, Contact>
    lateinit var receiverWithAmountList: MutableList<ReceiverWithAmount>
    lateinit var friendsList: Array<String?>

    override fun attachView(baseViewContract: DebtActivityContract.View) {
        super.attachView(baseViewContract)

        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()
        unsubscribeFromEventBus()
    }

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.AddDebtReceiverWithAmountListIsChanged -> {
                                val newAmount = `object`.myNewText
                                val positionInList = `object`.myPositionInList
                                Log.d("AddDebtReceiverWithAmountListIsChanged, newAmount = $newAmount, position = $positionInList")
                                receiverWithAmountList[positionInList].amount = newAmount.toFloat()
                            }
                            is Events.OnClickExpenseItemList -> {
                                getView()?.displayExpenseActivity(`object`.myDebtId, `object`.myExpenseId)
                            }
                        }
                    }
        }
    }

    private fun unsubscribeFromEventBus() {
        if (subscriptionToBus != null) {
            subscriptionToBus?.dispose()
            subscriptionToBus = null
        }
    }

    override fun viewIsReady() {
        Log.d("DebtActivity view id ready")
        //getView()?.getEmail()?.length!! > 0

        getView()?.hideProgress()

        Log.d("getting all contacts from db...")
        ContactsProvider().getAll(this)

        if (::debt.isInitialized) {
            ExpenseProvider().getExpensesForDebt(this, debt.uid.toString())
        }

        if (::friendsList.isInitialized && senderId != null) {
            Log.d("view is ready, set senderId = $senderId")
            getView()?.setSender(senderId!!)
        }
    }

    override fun viewIsPaused() {
        senderId = getView()?.getSender()
        //Log.d("view is paused, save senderId = $senderId")
    }

    override fun viewIsCreated() {
        super.viewIsCreated()
    }

    override fun onContactsListLoaded(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {

        Log.d("contacts list is loaded")

        if (contactsList.isEmpty()) {
            getView()?.showAlertAndGoToContacts("Please add at least one contact first")
        }

        friendsList = arrayOfNulls<String>(contactsList.size)
        var i = 0

        contactsIdToNameMap = mapOf<String, Contact>()
        contactsList.forEach { contact ->
            friendsList[i] = contact.alias.toString()
            contactsIdToNameMap.plus(Pair(contact.uid, contact))
            i = i + 1
        }

        getView()?.setupSenderSpinner(friendsList)

        if (senderId == null) {
            if (::debt.isInitialized && debt.senderId != null && debt.senderId!!.isNotEmpty()) {
                Log.d("setSender")
                getView()?.setSender(debt.senderId!!.toInt())
            }
        }
    }

    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
        getView()?.hideProgress()
    }

    override fun onDebtDeleted() {
        super.onDebtDeleted()
        getView()?.finishSelf()
    }

    override fun onDebtUpdated() {
        //getView()?.hideProgress()

        Log.d("debt is updated")
        //MainApplication.bus.send(Events.DebtIsAdded())
        getView()?.finishSelf()
    }

    override fun onDebtLoaded(debt: Debt) {
        super.onDebtLoaded(debt)

        Log.d("onDebtLoaded, display data...")
        this.debt = debt

        displayDebtData()

        ExpenseProvider().getExpensesForDebt(this, debt.uid.toString())
    }

    override fun onNoDebtWithIdExist() {
        super.onNoDebtWithIdExist()
        //ToDo ErrorHandling load debt draft
    }

    override fun onDebtDraftAdded() {
        super.onDebtDraftAdded()

        DebtsProvider().getDebtDraft(this)
    }

    override fun onDebtDraftLoaded(debt: Debt) {
        super.onDebtDraftLoaded(debt)

        Log.d("draft debt id = ${debt.uid}")

        this.debt = debt
        displayDebtData()

        ExpenseProvider().getExpensesForDebt(this, debt.uid.toString())
    }

    override fun onNoDebtDraftExist() {
        super.onNoDebtDraftExist()

        DebtsProvider().addDebtDraft(this)
    }

    override fun onExpensesForDebtListLoaded(expenseList: List<Expense>) {
        super.onExpensesForDebtListLoaded(expenseList)

        getView()?.hideProgress()

        expenseList.forEach { expense ->
            Log.d("expense: debtId = ${expense.debtId}, amount = ${expense.totalAmount}, receiversList = ${expense.receiversList}")
        }

        getView()?.initializeExpensesList(expenseList)
    }

    override fun onNoExpensesForDebt() {
        super.onNoExpensesForDebt()

        getView()?.hideProgress()
    }

    var addDebtButtonEnabled: Boolean = false

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        addDebtButtonEnabled = areFilled
    }

    override fun addButtonIsPressed() {

        Log.d("handle add button pressed - update debt")
        getView()?.hideKeyboard()
        getView()?.showProgress()


        debt.senderId = getView()?.getSender().toString()
        debt.spentAmount= getView()?.getAmount()

        if (getView()?.getDate() != null) {
            val formattedDate = DateFormatter().getTimestampFromFormattedDate(getView()?.getDate()!!)
            if (formattedDate != null) {
                debt.datetime = formattedDate.toString()
            }
        }

        debt.comment = getView()?.getComment()
        debt.status = "active"

        DebtsProvider().updateDebt(this, debt)
    }

    override fun date_edit_text_is_clicked() {
        getView()?.displayDatePickerDialog()
    }

    override fun addReceiverButtonIsPressed() {

        getView()?.displayExpenseActivity(debt.uid, null)

    }

    override fun debtIdIsReceiverFromMainActivity(debtId: Int?) {

        Log.d("debtIdIsReceiverFromMainActivity = $debtId")

        if (debtId != null && debtId != 0) {
            DebtsProvider().getDebtById(this, debtId)
        } else {
            DebtsProvider().getDebtDraft(this)
        }
    }

    override fun deleteButtonIsPressed() {
        DebtsProvider().deleteDebt(this, debt)
    }

    private fun displayDebtData() {
        if (senderId == null) {
            if (debt.senderId != null && ::friendsList.isInitialized) {
                Log.d("setSender")
                getView()?.setSender(debt.senderId!!.toInt())
            }
        }

        if (debt.spentAmount != null) {
            getView()?.setAmount(debt.spentAmount!!)
        }

        if (debt.datetime != null) {
                getView()?.setDate(DateFormatter().formatDateFromTimestamp(debt.datetime!!.toLong()))
            } else {
                TODO("VERSION.SDK_INT < N")
            }

        if (debt.comment != null) {
            getView()?.setComment(debt.comment!!)
        }
    }
}