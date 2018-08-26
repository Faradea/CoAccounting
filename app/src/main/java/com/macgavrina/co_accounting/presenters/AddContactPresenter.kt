package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddContactContract
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events

class AddContactPresenter: BasePresenter<AddContactContract.View>(), AddContactContract.Presenter, ContactsProvider.DatabaseCallback {

    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
        getView()?.hideProgress()
    }

    override fun onContactAdded() {
        getView()?.hideProgress()

        MainApplication.bus.send(Events.ContactIsAdded())
    }

    var addContactButtonEnabled: Boolean = false

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        addContactButtonEnabled = areFilled
        getView()?.setAddButtonEnabled(addContactButtonEnabled)
    }

    override fun viewIsReady() {

        addContactButtonEnabled = getView()?.getEmail()?.length!! > 0

        getView()?.setAddButtonEnabled(addContactButtonEnabled)
        getView()?.hideProgress()

    }

    override fun addButtonIsPressed() {
        getView()?.hideKeyboard()
        getView()?.showProgress()

        val contact:Contact = Contact()
        contact.email = getView()?.getEmail()
        contact.alias = getView()?.getAlias()

        ContactsProvider().addContact(this, contact)
    }
}