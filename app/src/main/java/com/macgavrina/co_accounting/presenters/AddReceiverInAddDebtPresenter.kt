package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddReceiverInAddDebtContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events

class AddReceiverInAddDebtPresenter: BasePresenter<AddReceiverInAddDebtContract.View>(), AddReceiverInAddDebtContract.Presenter, ContactsProvider.DatabaseCallback {

    var notSelectedContactsList = mutableListOf<Contact>()
    var selectedContactsList = mutableListOf<Contact>()

    override fun onDatabaseError() {
        Log.d("database error")
    }

    override fun onContactsListLoaded(contactsList: List<Contact>) {
        super.onContactsListLoaded(contactsList)

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
                            //selectedContactsList =
                            getView()?.initializeSelectedReceiversList(selectedContactsList)
                        }
                    }
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
}