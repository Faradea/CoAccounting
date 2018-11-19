package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.ExpenseReceiversWithAmountGroup
import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.DebtsProvider
import com.macgavrina.co_accounting.providers.ExpenseProvider
import com.macgavrina.co_accounting.providers.ReceiverForAmountProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import com.macgavrina.co_accounting.rxjava.Events

class AddDebtPresenter: BasePresenter<AddDebtContract.View>(), AddDebtContract.Presenter, DebtsProvider.DatabaseCallback, ContactsProvider.DatabaseCallback, ExpenseProvider.DatabaseCallback {


    lateinit var contactsIdToNameMap: Map<String, Contact>
    lateinit var receiverWithAmountList: MutableList<ReceiverWithAmount>
    lateinit var friendsList: Array<String?>

    override fun attachView(baseViewContract: AddDebtContract.View) {
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
                    }
                }
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

    override fun onExpenseListLoaded(expenseList: List<Expense>) {
        super.onExpenseListLoaded(expenseList)

        getView()?.initializeExpensesList(expenseList)
    }

    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
        getView()?.hideProgress()
    }

    override fun onDebtAdded() {
        getView()?.hideProgress()

        MainApplication.bus.send(Events.DebtIsAdded())
    }

    var addDebtButtonEnabled: Boolean = false

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        addDebtButtonEnabled = areFilled
        getView()?.setAddButtonEnabled(addDebtButtonEnabled)
    }

    override fun viewIsReady() {

        //ToDo написать условия при который кнопка "добавить" активна
        addDebtButtonEnabled = true
                //getView()?.getEmail()?.length!! > 0

        getView()?.setAddButtonEnabled(addDebtButtonEnabled)
        getView()?.hideProgress()

        ContactsProvider().getAll(this)

        ExpenseProvider().getAll(this)

    }

    override fun addButtonIsPressed() {

        getView()?.hideKeyboard()
        getView()?.showProgress()

        val debt = Debt()
        debt.receiverId = getView()?.getReceiver()
        debt.spentAmount= getView()?.getAmount()
        debt.datetime = getView()?.getDate()
        debt.comment = getView()?.getComment()

        //ToDo MainActivity should pass eventId here. If no expenseId is passed - list is saved with eventId = "-1" (draft)
        //Draft (event with id = -1 should ALWAYS exist but may have all values is set to null

        DebtsProvider().addDebt(this, debt)
    }

    override fun addReceiverButtonIsPressed() {
        getView()?.hideKeyboard()

//        val receiverWithAmount = RecieverWithAmount("TestName", 220.0f, receiverWithAmountList.size)
//        receiverWithAmountList.add(receiverWithAmount)
//
//        getView()?.initializeReceiversList(receiverWithAmountList, friendsList)
        MainApplication.bus.send(Events.AddReceiverButtonInAddDebtFragment())
    }
}