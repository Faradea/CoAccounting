package com.macgavrina.co_accounting.providers

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers


class ContactsProvider() {

    fun addContact(contact: Contact) {
        Observable.just(MainApplication.db)
                .subscribeOn(Schedulers.io())
                .subscribe { db -> MainApplication.db.contactDAO().insertContact(contact) }
        Log.d("contact is saved in DB")
    }



/*
    fun addContact(contact:Contact) : Completable = Completable.fromCallable {
        MainApplication.db.contactDAO().insertContact(contact)
        Log.d("contact is saved in DB")
    }
*/


/*    fun getAllContacts():List<Contact> {
        var contactList:List<Contact>
        Observable.just(MainApplication.db)
                .subscribeOn(Schedulers.io())
                .subscribe { db ->
                    contactList = MainApplication.db.contactDAO().all
                }
        return contactList
        Log.d("contact is saved in DB")
    }*/

/*    fun loadUser(callback: LoadUserCallback){
        val loadUserTask = LoadUserTask(callback)
        loadUserTask.execute()
    }*/


/*    interface LoadUserCallback {
        fun onLoad(user: User)
    }
    */
}