package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.EditContactContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class EditContactPresenter: BasePresenter<EditContactContract.View>(), EditContactContract.Presenter, ContactsProvider.DatabaseCallback{

    override fun onContactLoaded(loadedContact: Contact) {
        contact = loadedContact
        getView()?.hideProgress()
        getView()?.displayContactData(contact.alias!!, contact.email!!)
    }

    override fun onContactDeleted() {
        getView()?.displayToast("Contact is deleted")
        getView()?.hideProgress()
        MainApplication.bus.send(Events.ContactEditingIsFinished())
    }

    override fun onContactUpdated() {
        getView()?.displayToast("Changes are saved")
        getView()?.hideProgress()
        MainApplication.bus.send(Events.ContactEditingIsFinished())
    }

    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
        getView()?.hideProgress()
    }

    override fun viewIsReady() {
    }

    var saveButtonEnabled: Boolean = false
    lateinit var contact:Contact

    override fun aliasIsChanged() {
            if (saveButtonEnabled == false) {
                saveButtonEnabled = !getView()?.getAliasFromEditText().equals(contact.alias)
            }
            getView()?.setSaveButtonEnabled(saveButtonEnabled)
        }

    override fun viewIsReady(uid: String) {

        getView()?.setSaveButtonEnabled(saveButtonEnabled)
        getView()?.showProgress()

        if (uid.length != 0) {
            ContactsProvider().getContactById(this, uid)
        }
        getView()?.setSaveButtonEnabled(saveButtonEnabled)
        getView()?.hideProgress()

    }

    override fun saveButtonIsPressed() {

        saveButtonEnabled = false
        getView()?.setSaveButtonEnabled(saveButtonEnabled)
        getView()?.hideKeyboard()
        getView()?.showProgress()

        contact.alias = getView()?.getAliasFromEditText()

        Log.d("save data")
        ContactsProvider().updateContact(this, contact)

    }

    override fun deleteButtonIsPressed() {
        getView()?.showProgress()

        Log.d("delete data")
        ContactsProvider().deleteContact(this, contact)
    }
}