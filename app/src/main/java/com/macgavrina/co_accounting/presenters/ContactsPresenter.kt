package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.Contact
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ContactsPresenter: BasePresenter<ContactsContract.View>(), ContactsContract.Presenter, ContactsProvider.DatabaseCallback {

    override fun onContactsListLoaded(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
        getView()?.hideProgress()
        getView()?.initializeList(contactsList)
    }
    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
    }

    override fun viewIsReady() {

        Log.d("view is ready")

        getView()?.showProgress()

        ContactsProvider().getAll(this)

        getView()?.hideProgress()

    }

    override fun addContactButtonIsPressed() {
        Log.d("is pressed")
        MainApplication.bus.send(Events.AddContact())
    }

    override fun contactItemIsSelected(selectedContactId: Int) {
        Log.d("selectedContactId = ${selectedContactId}")
    }

}