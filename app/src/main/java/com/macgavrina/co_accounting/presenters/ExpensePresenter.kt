package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.ExpenseProvider
import com.macgavrina.co_accounting.providers.ReceiverForAmountProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.disposables.Disposable
import java.text.DecimalFormat

class ExpensePresenter: BasePresenter<AddReceiverInAddDebtContract.View>(), AddReceiverInAddDebtContract.Presenter, ContactsProvider.DatabaseCallback, ReceiverForAmountProvider.DatabaseCallback, ExpenseProvider.DatabaseCallback {

    private var subscriptionToBus: Disposable? = null

    var debtId: Int? = null
    var expenseId: Int? = null
    var expense: Expense? = null
    var amountPerPerson: String = "0"
    var contactsList: List<Contact>? = null
    var notSelectedContactsList = mutableListOf<Contact>()
    var selectedContactsList = mutableListOf<Contact>()
    var receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

    var receiversWithAmountListIsLoaded = false
    var contactsListIsLoaded = false

    override fun onDatabaseError() {
        Log.d("database error")
    }

    override fun onReceiverWithAmountListAdded() {
        super.onReceiverWithAmountListAdded()
        Log.d("receiver with amount list is added")

        getView()?.finishSelf()
        //MainApplication.bus.send(Events.ReceiversWithAmountInAddDebtIsSaved())
    }

    override fun onReceiversWithAmountListForExpensesDeleted() {
        super.onReceiversWithAmountListForExpensesDeleted()

        receiversWithAmountList?.forEach { receiversWithAmount ->
            receiversWithAmount.expenseId = expense?.uid.toString()
        }

        ReceiverForAmountProvider().addReceiverWithAmountList(this, receiversWithAmountList!! )
    }

    override fun onContactsListLoaded(contactsList: List<Contact>) {
        super.onContactsListLoaded(contactsList)

        this.contactsList = contactsList
        amountPerPerson = "0"
        notSelectedContactsList.clear()
        selectedContactsList.clear()
        contactsList.forEach { contact ->
            notSelectedContactsList.add(contact)
        }
        getView()?.initializeNotSelectedReceiversList(contactsList)

        contactsListIsLoaded = true
        updateReceiverWithAmountListWithDataFromDB()
    }

    override fun onExpenseAdded() {
        super.onExpenseAdded()

        ExpenseProvider().getLastExpenseId(this)
    }

    override fun onExpenseUpdated() {
        super.onExpenseUpdated()

        ReceiverForAmountProvider().deleteReceiversWithAmountForExpense(this, expense?.uid.toString())
    }

    override fun onGetLastExpenseId(uid: Int) {
        super.onGetLastExpenseId(uid)

        receiversWithAmountList?.forEach { receiversWithAmount ->
            receiversWithAmount.expenseId = uid.toString()
        }

        Log.d("saving receiverWithAmountList for expenseId = $uid")
        ReceiverForAmountProvider().addReceiverWithAmountList(this, receiversWithAmountList!! )
    }

    override fun onExpenseByIdLoaded(expense: Expense) {
        super.onExpenseByIdLoaded(expense)
        this.expense = expense

        getView()?.showDeleteButton()

        getView()?.setAmount(expense.totalAmount)

        ReceiverForAmountProvider().getReceiversWithAmountForExpense(this, expense.uid.toString())

    }

    override fun onNoExpenseWithRequestedId() {
        //ToDo ErrorHandling
    }

    override fun onExpenseDeleted() {
        super.onExpenseDeleted()
        getView()?.finishSelf()
        //MainApplication.bus.send(Events.HideAddReceiverInAddDebtFragment(true))
    }

    override fun onReceiversWithAmountForExpenseListLoaded(receiversWithAmountList: List<ReceiverWithAmountForDB>) {
        super.onReceiversWithAmountForExpenseListLoaded(receiversWithAmountList)

        this.receiversWithAmountList = receiversWithAmountList as MutableList<ReceiverWithAmountForDB>
        receiversWithAmountListIsLoaded = true
        updateReceiverWithAmountListWithDataFromDB()

    }

    override fun attachView(baseViewContract: AddReceiverInAddDebtContract.View) {
        super.attachView(baseViewContract)

        subscribeToEventBus()

        ContactsProvider().getAll(this)

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
                            is Events.NewContactIsAddedToSelectedReceiversList -> {
                                val contact = `object`.myContact
                                notSelectedContactsList.remove(contact)
                                selectedContactsList.add(contact!!)

                                if (getView()?.getAmount() != null) {
                                    amountPerPerson = DecimalFormat("##.##").format(getView()?.getAmount()!! / selectedContactsList.size)
                                } else {
                                    amountPerPerson = "0"
                                }
                                getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
                                getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                            }
                            is Events.onClickSelectedReceiverOnAddExpenseFragment -> {
                                val contact = `object`.myContact
                                selectedContactsList.remove(contact)
                                notSelectedContactsList.add(contact)

                                if (getView()?.getAmount() != null) {
                                    amountPerPerson = DecimalFormat("##.##").format(getView()?.getAmount()!! / selectedContactsList.size)
                                } else {
                                    amountPerPerson = "0"
                                }
                                getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
                                getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)

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

    override fun debtIdIsReceiverFromMainActivity(debtId: Int) {
        this.debtId = debtId
    }

    override fun expenseIdIsReceivedFromMainActivity(expenseId: Int) {
        this.expenseId = expenseId

        ExpenseProvider().getExpenseById(this, expenseId)
    }

    override fun amountIsEdited(newAmount: Float) {
        if (selectedContactsList.isNotEmpty()) {
            amountPerPerson = DecimalFormat("##.##").format(newAmount/selectedContactsList.size)
            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
        }

    }

    override fun viewIsReady() {

//        addDebtButtonEnabled = true
//        //getView()?.getEmail()?.length!! > 0
//
//        getView()?.setAddButtonEnabled(addDebtButtonEnabled)
//        getView()?.hideProgress()
//
        ContactsProvider().getAll(this)
        getView()?.hideDeleteButton()

    }

    override fun cancelButtonInToolbarIsClicked() {
        getView()?.hideKeyboard()
        MainApplication.bus.send(Events.HideAddReceiverInAddDebtFragment(false))
    }

    override fun saveButtonIsPressed() {

        getView()?.hideKeyboard()

        var receiversListString = ""

        receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()

        selectedContactsList.forEach { contact ->

            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.amount = amountPerPerson
            receiverWithAmount.contactId = contact.uid.toString()
            receiversWithAmountList.add(receiverWithAmount)

            if (receiversListString == "") {
                receiversListString = contact.alias.toString()
            } else {
                receiversListString = "$receiversListString, ${contact.alias.toString()}"
            }
        }


        if (expense == null) {
            expense = Expense()
            expense!!.totalAmount = DecimalFormat("##.##").format(getView()?.getAmount())
            expense!!.receiversList = receiversListString
            expense!!.debtId = debtId

            Log.d("add expense: debtId = ${expense!!.debtId}, receiversList = ${expense!!.receiversList}, amount = ${expense!!.totalAmount}")

            ExpenseProvider().addExpense(this, expense!!)

        } else {
            expense!!.totalAmount = DecimalFormat("##.##").format(getView()?.getAmount())
            expense!!.receiversList = receiversListString
            expense!!.debtId = debtId

            ExpenseProvider().updateExpense(this, expense!!)
        }
    }

    override fun deleteButtonIsPressed() {
        ExpenseProvider().deleteExpense(this, expense!!)
    }

    private fun updateReceiverWithAmountListWithDataFromDB() {

        if (receiversWithAmountListIsLoaded && contactsListIsLoaded) {

            Log.d("go through receiversWithAmountList with ${receiversWithAmountList.size} items")
            receiversWithAmountList.forEach { receiversWithAmount ->

                val contactId = receiversWithAmount.contactId
                Log.d("go throw receiversWithAmountList, for contactId = $contactId")
                val contact = contactsList!![contactId!!.toInt()-1]

                selectedContactsList.add(contact)
                notSelectedContactsList.remove(contact)
            }

            if (getView()?.getAmount() != null) {
                amountPerPerson =  DecimalFormat("##.##").format(getView()?.getAmount()!! / selectedContactsList.size)

            } else {
                amountPerPerson = "0"
            }

            getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)
            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)

        }
    }
}