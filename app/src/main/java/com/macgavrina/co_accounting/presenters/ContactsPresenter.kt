package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ContactsPresenter: BasePresenter<ContactsContract.View>(), ContactsContract.Presenter {

    private var subscriptionToBus: Disposable? = null

    override fun attachView(baseViewContract: ContactsContract.View) {
        super.attachView(baseViewContract)
        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
    }

    override fun viewIsReady() {
        getAndDisplayAllContacts()
    }

    override fun addContactButtonIsPressed() {
        Log.d("Add new contact button is pressed")
        MainApplication.bus.send(Events.AddContact())
    }

    override fun contactItemIsSelected(selectedContactId: Int) {
        Log.d("User clicked on contact, selectedContactId = ${selectedContactId}")
    }

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.DeletedContactIsRestored -> {
                                Log.d("catch Events.DeletedContactIsRestored event, updating contacts list...")
                                getAndDisplayAllContacts()
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

    private fun getAndDisplayAllContacts() {
        getView()?.showProgress()
        MainApplication.db.contactDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Contact>>() {
                    override fun onSuccess(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
                        Log.d("Contacts list is received from DB, size = ${contactsList.size}")
                        getView()?.hideProgress()
                        getView()?.initializeList(contactsList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error loading contacts from DB, $e")
                        getView()?.hideProgress()
                        getView()?.displayToast("Database error")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                    }
                })
    }

}