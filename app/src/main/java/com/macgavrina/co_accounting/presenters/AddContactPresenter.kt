package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddContactContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.DebtsProvider
import com.macgavrina.co_accounting.providers.ReceiverForAmountProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events

class AddContactPresenter: BasePresenter<AddContactContract.View>(), AddContactContract.Presenter, ContactsProvider.DatabaseCallback, DebtsProvider.DatabaseCallback, ReceiverForAmountProvider.DatabaseCallback {

    lateinit var contact: Contact

    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
        getView()?.hideProgress()
    }

    override fun onContactAdded() {
        getView()?.hideProgress()

        getView()?.displayToast("Contact is added")
        getView()?.finishSelf()
    }


    override fun viewIsReady() {

        getView()?.hideProgress()
    }

    override fun addButtonIsPressed() {
        getView()?.hideKeyboard()
        getView()?.showProgress()

        if (::contact.isInitialized) {
            contact.email = getView()?.getEmail()
            contact.alias = getView()?.getAlias()
            ContactsProvider().updateContact(this, contact)
        } else {
            contact = Contact()
            contact.email = getView()?.getEmail()
            contact.alias = getView()?.getAlias()
            ContactsProvider().addContact(this, contact)
        }
    }

    override fun onContactLoaded(loadedContact: Contact) {
        contact = loadedContact
        getView()?.hideProgress()
        getView()?.displayContactData(contact.alias!!, contact.email!!)
    }

    override fun onContactDeleted() {
        getView()?.displayToast("Contact is deleted")
        getView()?.finishSelf()
    }

    override fun onContactUpdated() {
        getView()?.displayToast("Changes are saved")
        getView()?.finishSelf()

    }


    override fun contactIdIsReceiverFromMainActivity(contactId: String?) {
        if (contactId != null) {
            ContactsProvider().getContactById(this, contactId)
        } else {
            getView()?.hideDeleteButton()
        }
    }

    override fun deleteButtonIsPressed() {
        //getView()?.showProgress()

        DebtsProvider().checkDebtsForContact(this, contact.uid.toString())

    }

    override fun onDebtsForContactChecked(list: List<Debt>) {
        super.onDebtsForContactChecked(list)

        if (list.isEmpty()) {

            ReceiverForAmountProvider().checkReceiverWithAmountForContact(this, contact.uid.toString())
        } else {
            getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
        }
    }

    override fun onCheckReceiversForContact(count: Int) {
        super.onCheckReceiversForContact(count)

        if (count == 0) {
            Log.d("delete data")
            ContactsProvider().deleteContact(this, contact)
        } else {
            getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
        }
    }

}
