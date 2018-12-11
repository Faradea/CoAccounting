package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.disposables.Disposable

class ContactsPresenter: BasePresenter<ContactsContract.View>(), ContactsContract.Presenter, ContactsProvider.DatabaseCallback {

    private var subscriptionToBus: Disposable? = null

    override fun onContactsListLoaded(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
        getView()?.hideProgress()
        getView()?.initializeList(contactsList)
    }
    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
    }


    override fun attachView(baseViewContract: ContactsContract.View) {
        super.attachView(baseViewContract)
        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
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

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.DeletedContactIsRestored -> {

                                getView()?.showProgress()

                                ContactsProvider().getAll(this)

                                getView()?.hideProgress()
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

}