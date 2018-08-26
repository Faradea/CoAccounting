package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.Contact
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ContactsPresenter: BasePresenter<ContactsContract.View>(), ContactsContract.Presenter {
    override fun viewIsReady() {

        Log.d("view is ready")

        getView()?.showProgress()

        //ToDo перенести в ContactProvider
        MainApplication.db.contactDAO().getAll
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<com.macgavrina.co_accounting.room.Contact>>() {
                    override fun onSuccess(t: List<com.macgavrina.co_accounting.room.Contact>) {
                        getView()?.initializeList(t)
                    }

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                            Log.d("nothing")
                        }
                    })

        getView()?.hideProgress()

    }

    override fun addContactButtonIsPressed() {
        Log.d("is pressed")
        MainApplication.bus.send(Events.AddContact())
    }

    override fun deleteContactsButtonIsPressed(selectedContactsIds: List<Int>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun contactItemIsSelected(selectedContactId: Int) {
        Log.d("selectedContactId = ${selectedContactId}")
    }

}