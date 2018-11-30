package com.macgavrina.co_accounting.presenters

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

class DebtActivityPresenter:BasePresenter<DebtActivityContract.View>(), DebtActivityContract.Presenter, DebtsProvider.DatabaseCallback, ContactsProvider.DatabaseCallback, ExpenseProvider.DatabaseCallback {

    lateinit var debt: Debt
    lateinit var contactsIdToNameMap: Map<String, Contact>
    lateinit var receiverWithAmountList: MutableList<ReceiverWithAmount>
    lateinit var friendsList: Array<String?>

    override fun attachView(baseViewContract: DebtActivityContract.View) {
        super.attachView(baseViewContract)

        MainApplication
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
                        is Events.AddDebtFragmentRequiresRefresh -> {
                            //ToDo считать обновления из базы и обновить список во фрагменте
                        }
                        is Events.OnClickExpenseItemList -> {
                            getView()?.displayExpenseActivity(`object`.myDebtId, `object`.myExpenseId)
                        }
                    }
                }
    }


    override fun viewIsReady() {
        Log.d("DebtActivity view id ready")
        //ToDo написать условия при который кнопка "добавить" активна
        addDebtButtonEnabled = true
        //getView()?.getEmail()?.length!! > 0

        getView()?.setAddButtonEnabled(addDebtButtonEnabled)
        getView()?.hideProgress()

        ContactsProvider().getAll(this)

        if (::debt.isInitialized) {
            ExpenseProvider().getExpensesForDebt(this, debt.uid.toString())
        }
    }

    override fun viewIsCreated() {
        super.viewIsCreated()
    }

    override fun onContactsListLoaded(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {

        //ToDo добавлять в список первым пунктом себя
        friendsList = arrayOfNulls<String>(contactsList.size)
        var i = 0

        contactsIdToNameMap = mapOf<String, Contact>()
        contactsList.forEach { contact ->
            friendsList[i] = contact.alias.toString()
            contactsIdToNameMap.plus(Pair(contact.uid, contact))
            i = i + 1
        }

        getView()?.setupSenderSpinner(friendsList)
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
        getView()?.hideProgress()

        //MainApplication.bus.send(Events.DebtIsAdded())
        getView()?.finishSelf()
    }

    override fun onDebtLoaded(debt: Debt) {
        super.onDebtLoaded(debt)

        //getView()?.setSender(debt.)

        this.debt = debt

        if (debt.spentAmount != null) {
            getView()?.setAmount(debt.spentAmount!!)
        }

        if (debt.datetime != null) {
            getView()?.setDate(debt.datetime!!)
        }

        if (debt.comment != null) {
            getView()?.setComment(debt.comment!!)
        }

        ExpenseProvider().getExpensesForDebt(this, debt.uid.toString())
    }

    override fun onNoDebtWithIdExist() {
        super.onNoDebtWithIdExist()

        //ToDo load debt draft
    }

    override fun onDebtDraftAdded() {
        super.onDebtDraftAdded()

        DebtsProvider().getDebtDraft(this)
    }

    override fun onDebtDraftLoaded(debt: Debt) {
        super.onDebtDraftLoaded(debt)

        Log.d("draft debt id = ${debt.uid}")

        this.debt = debt
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
        getView()?.setAddButtonEnabled(addDebtButtonEnabled)
    }

    override fun addButtonIsPressed() {

        Log.d("handle add button pressed")
        getView()?.hideKeyboard()
        getView()?.showProgress()

        debt.receiverId = getView()?.getReceiver()
        debt.spentAmount= getView()?.getAmount()
        debt.datetime = getView()?.getDate()
        debt.comment = getView()?.getComment()
        debt.status = "active"

        DebtsProvider().updateDebt(this, debt)
    }

    override fun addReceiverButtonIsPressed() {

        //ToDo переделать на вызов активити
//        getView()?.hideKeyboard()
//
//        val receiverWithAmount = RecieverWithAmount("TestName", 220.0f, receiverWithAmountList.size)
//        receiverWithAmountList.add(receiverWithAmount)
//
//        getView()?.initializeReceiversList(receiverWithAmountList, friendsList)
//        MainApplication.bus.send(Events.AddReceiverButtonInAddDebtFragment(debt.uid))

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
}