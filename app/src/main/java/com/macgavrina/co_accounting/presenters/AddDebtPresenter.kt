package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.RecieverWithAmount
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.DebtsProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events

class AddDebtPresenter: BasePresenter<AddDebtContract.View>(), AddDebtContract.Presenter, DebtsProvider.DatabaseCallback, ContactsProvider.DatabaseCallback {


    lateinit var receiverWithAmountList: MutableList<RecieverWithAmount>
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
                    }
                }
    }

    override fun onContactsListLoaded(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {

        //ToDo добавлять в список первым пунктом себя
        friendsList = arrayOfNulls<String>(contactsList.size)
        var i = 0

        contactsList.forEach { contact ->
            friendsList[i] = contact.alias.toString()
            i = i + 1
        }

        getView()?.setupSenderSpinner(friendsList)

        val receiverWithAmount = RecieverWithAmount("TestName", 500.0f, 0)
        receiverWithAmountList = mutableListOf(receiverWithAmount)

        getView()?.initializeReceiversList(receiverWithAmountList, friendsList)
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

    }

    override fun addButtonIsPressed() {
        getView()?.hideKeyboard()
        getView()?.showProgress()

        val debt = Debt()
        debt.sender = getView()?.getSender()
        debt.receiver = getView()?.getReceiver()
        debt.amount = getView()?.getAmount()
        debt.datetime = getView()?.getDate()
        debt.comment = getView()?.getComment()

        DebtsProvider().addDebt(this, debt)
    }

    override fun addReceiverButtonIsPressed() {
        getView()?.hideKeyboard()

        val receiverWithAmount = RecieverWithAmount("TestName", 220.0f, receiverWithAmountList.size)
        receiverWithAmountList.add(receiverWithAmount)

        getView()?.initializeReceiversList(receiverWithAmountList, friendsList)
    }
}