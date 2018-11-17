package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.ExpenseProvider
import com.macgavrina.co_accounting.providers.ReceiverForAmountProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDBDAO
import com.macgavrina.co_accounting.rxjava.Events
import java.lang.Exception

class AddReceiverInAddDebtPresenter: BasePresenter<AddReceiverInAddDebtContract.View>(), AddReceiverInAddDebtContract.Presenter, ContactsProvider.DatabaseCallback, ReceiverForAmountProvider.DatabaseCallback, ExpenseProvider.DatabaseCallback {
    //ToDo переименовать в Expense

    var amountPerPerson: Float = 0F
    var notSelectedContactsList = mutableListOf<Contact>()
    var selectedContactsList = mutableListOf<Contact>()

    override fun onDatabaseError() {
        Log.d("database error")
    }

    override fun onReceiverWithAmountListAdded() {
        super.onReceiverWithAmountListAdded()
        Log.d("receiver with amount list is added")

        MainApplication.bus.send(Events.ReceiversWithAmountInAddDebtIsAdded())
    }

    override fun onExpenseAdded() {
        super.onExpenseAdded()

        //ToDo how to get expenseId to insert it?
        ReceiverForAmountProvider().addReceiverWithAmountList(this, receiversWithAmountList )
    }

    override fun onContactsListLoaded(contactsList: List<Contact>) {
        super.onContactsListLoaded(contactsList)

        amountPerPerson = 0F
        notSelectedContactsList.clear()
        selectedContactsList.clear()
        contactsList.forEach { contact ->
            notSelectedContactsList.add(contact)
        }
        getView()?.initializeNotSelectedReceiversList(contactsList)
    }

    override fun attachView(baseViewContract: AddReceiverInAddDebtContract.View) {
        super.attachView(baseViewContract)

        ContactsProvider().getAll(this)

        MainApplication
                .bus
                .toObservable()
                .subscribe { `object` ->
                    when (`object`) {
                        is Events.NewContactIsAddedToSelectedReceiversList -> {
                            val contact = `object`.myContact
                            notSelectedContactsList.remove(contact)
                            getView()?.initializeNotSelectedReceiversList(notSelectedContactsList)

                            selectedContactsList.add(contact!!)
                            if (getView()?.getAmount() != null) {
                                amountPerPerson = getView()?.getAmount()!! / selectedContactsList.size
                            } else {
                                amountPerPerson = 0F
                            }
                            getView()?.initializeSelectedReceiversList(selectedContactsList, amountPerPerson)
                        }
                    }
                }
    }


    override fun amountIsEdited(newAmount: Float) {
        if (selectedContactsList.isNotEmpty()) {
            amountPerPerson = newAmount/selectedContactsList.size
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

    }

    override fun cancelButtonInToolbarIsClicked() {
        getView()?.hideKeyboard()
        MainApplication.bus.send(Events.CancelButtonInAddReceiverInAddDebtFragment())
    }

    override fun saveButtonIsPressed() {

        getView()?.hideKeyboard()

        //ToDo Should be done in transaction
        var receiversListString = ""

        val receiversWithAmountList = mutableListOf<ReceiverWithAmountForDB>()
        selectedContactsList.forEach { contact ->

            val receiverWithAmount = ReceiverWithAmountForDB()
            receiverWithAmount.amount = amountPerPerson.toString()
            receiverWithAmount.contactId = contact.uid.toString()
            receiversWithAmountList.add(receiverWithAmount)

            if (receiversListString == "") {
                receiversListString = contact.alias.toString()
            } else {
                receiversListString = "$receiversListString, ${contact.alias.toString()}"
            }
        }


        val expense = Expense()
        expense.totalAmount = getView()?.getAmount().toString()
        expense.receiversList = receiversListString

        ExpenseProvider().addExpense(this, expense)

    }
}