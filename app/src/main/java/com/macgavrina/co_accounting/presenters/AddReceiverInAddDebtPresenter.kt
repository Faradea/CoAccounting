package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.room.Contact

class AddReceiverInAddDebtPresenter: BasePresenter<AddReceiverInAddDebtContract.View>(), AddReceiverInAddDebtContract.Presenter, ContactsProvider.DatabaseCallback {

    override fun onDatabaseError() {
        Log.d("database error")
    }

    override fun onContactsListLoaded(contactsList: List<Contact>) {
        super.onContactsListLoaded(contactsList)
        getView()?.initializeNotSelectedReceiversList(contactsList)
    }

    override fun attachView(baseViewContract: AddReceiverInAddDebtContract.View) {
        super.attachView(baseViewContract)

        ContactsProvider().getAll(this)

//        MainApplication
//                .bus
//                .toObservable()
//                .subscribe { `object` ->
//                    when (`object`) {
//                        is Events.AddDebtReceiverWithAmountListIsChanged -> {
//                            val newAmount = `object`.myNewText
//                            val positionInList = `object`.myPositionInList
//                            Log.d("AddDebtReceiverWithAmountListIsChanged, newAmount = $newAmount, position = $positionInList")
//                            receiverWithAmountList[positionInList].amount = newAmount.toFloat()
//                        }
//                    }
//                }
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
}