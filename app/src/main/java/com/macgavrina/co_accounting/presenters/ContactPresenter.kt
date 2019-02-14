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

class ContactPresenter: BasePresenter<AddContactContract.View>(), AddContactContract.Presenter {

    lateinit var contact: Contact

    override fun viewIsReady() {
        getView()?.hideProgress()
    }

    override fun doneButtonIsPressed() {

        Log.d("Done button is pressed")

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
                            Log.d("Contact is updated")
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
                            Log.d("Contact is added")
                            getView()?.hideProgress()
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

        Log.d("ContactId is received from main activity, = $contactId")

//        if (contactId != null) {
//
//            MainApplication.db.contactDAO().getContactByIds(contactId)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(object : DisposableMaybeObserver<Contact>() {
//                        override fun onSuccess(loadedContact: com.macgavrina.co_accounting.room.Contact) {
//                            contact = loadedContact
//                            getView()?.hideProgress()
//                            getView()?.displayContactData(contact.alias!!, contact.email!!)
//                        }
//
//                        override fun onError(e: Throwable) {
//                            Log.d(e.toString())
//                        }
//
//                        override fun onComplete() {
//                        }
//                    })
//        } else {
//            getView()?.hideDeleteButton()
//        }
    }

    override fun deleteButtonIsPressed() {

        Log.d("Delete button is pressed")

        Log.d("Check if contact is used for debts as sender...")
        MainApplication.db.debtDAO().checkDebtsForContact(contact.uid.toString(), "active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onComplete() {
                    }

                    override fun onSuccess(list: List<Debt>) {
                        if (list.isEmpty()) {

                            Log.d("Contact isn't used as sender in debts, checking if he is present in expenses...")
                            MainApplication.db.receiverWithAmountForDBDAO().checkReceiverWithAmountForContact(contact.uid.toString())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSubscribe(d: Disposable) {
                                        }

                                        override fun onSuccess(count: Int) {
                                            if (count == 0) {
                                                Log.d("Contacts hasn't been used in debts and expenses, delete ir...")
                                                deleteContact(contact)
                                            } else {
                                                getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
                                            }
                                        }

                                        override fun onError(e: Throwable) {
                                            Log.d("Error getting receivers with amount from DB, $e")
                                        }

                                    })
                        } else {
                            getView()?.displayAlert("Contact can't be deleted until it presents in debts", "Contact can't be deleted")
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error getting debts for contact from db, $e")
                    }
                })

    }



    private fun deleteContact(contact: Contact) {

//        Completable.fromAction {
//            MainApplication.db.contactDAO().deleteContact(contact.uid, "deleted") }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : CompletableObserver {
//                    override fun onSubscribe(d: Disposable) {
//
//                    }
//
//                    override fun onComplete() {
//                        Log.d("Contact is deleted")
//                        MainApplication.bus.send(Events.ContactIsDeleted(contact))
//                        getView()?.finishSelf()
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.d("Error deleting contact, $e")
//                    }
//                })
    }

}
