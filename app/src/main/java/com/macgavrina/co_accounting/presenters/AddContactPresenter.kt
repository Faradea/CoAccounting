package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.AddContactContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class AddContactPresenter: BasePresenter<AddContactContract.View>(), AddContactContract.Presenter {

    lateinit var contact: Contact

    override fun viewIsReady() {

        getView()?.hideProgress()
    }

    override fun addButtonIsPressed() {
        getView()?.hideKeyboard()

        getView()?.showProgress()

        if (::contact.isInitialized) {
            contact.email = getView()?.getEmail()
            contact.alias = getView()?.getAlias()

            Completable.fromAction { MainApplication.db.contactDAO().updateContact(contact) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onComplete() {
                            getView()?.finishSelf()
                        }

                        override fun onError(e: Throwable) {
                            Log.d("Error updating contact, $e")
                            getView()?.displayToast("Database error")
                            getView()?.hideProgress()
                        }
                    })

        } else {
            contact = Contact()
            contact.email = getView()?.getEmail()
            contact.alias = getView()?.getAlias()

            Completable.fromAction {
                MainApplication.db.contactDAO().insertContact(contact)
            }.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {}

                        override fun onComplete() {
                            getView()?.hideProgress()
                            //getView()?.displayToast("Contact is added")
                            getView()?.finishSelf()
                        }

                        override fun onError(e: Throwable) {
                            Log.d("Error adding contact, $e")
                            getView()?.displayToast("Database error")
                            getView()?.hideProgress()
                        }
                    })
        }
    }

    override fun contactIdIsReceiverFromMainActivity(contactId: String?) {
        if (contactId != null) {

            MainApplication.db.contactDAO().loadContactByIds(contactId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableMaybeObserver<Contact>() {
                        override fun onSuccess(loadedContact: com.macgavrina.co_accounting.room.Contact) {
                            contact = loadedContact
                            getView()?.hideProgress()
                            getView()?.displayContactData(contact.alias!!, contact.email!!)
                        }

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                            Log.d("nothing")
                        }
                    })
        } else {
            getView()?.hideDeleteButton()
        }
    }

    override fun deleteButtonIsPressed() {
        //getView()?.showProgress()

        MainApplication.db.debtDAO().checkDebtsForContact(contact.uid.toString(), "active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onComplete() {
                    }

                    override fun onSuccess(list: List<Debt>) {
                        if (list.isEmpty()) {
                            checkReceiversWithAmountForContact(contact.uid.toString())
                        } else {
                            getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }
                })

    }


    private fun checkReceiversWithAmountForContact(contactId: String) {

        MainApplication.db.receiverWithAmountForDBDAO().checkReceiverWithAmountForContact(contactId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(count: Int) {
                        if (count == 0) {
                            Log.d("delete data")

                            deleteContact(contact)

                        } else {
                            getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                })
    }

    private fun deleteContact(contact: Contact) {

        Completable.fromAction {
            MainApplication.db.contactDAO().deleteContact(contact.uid.toString(), "deleted") }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onComplete() {
                        MainApplication.bus.send(Events.ContactIsDeleted(contact))
                        getView()?.finishSelf()
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error deleting contact, $e")
                    }
                })
    }

}
