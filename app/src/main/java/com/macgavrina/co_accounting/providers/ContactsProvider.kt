package com.macgavrina.co_accounting.providers

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.Disposable
import io.reactivex.CompletableObserver
import io.reactivex.functions.Action
import io.reactivex.observers.DisposableMaybeObserver
import android.os.Bundle
import com.macgavrina.co_accounting.customsync.SyncService
import com.macgavrina.co_accounting.view.MainActivity


class ContactsProvider() {

    //ToDo REFACT сделать singleton (см. пример в SyncService): этот и все остальные провайдеры

    fun getAll(databaseCallback: DatabaseCallback) {
        MainApplication.db.contactDAO().getAll
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<com.macgavrina.co_accounting.room.Contact>>() {
                    override fun onSuccess(t: List<com.macgavrina.co_accounting.room.Contact>) {
                        databaseCallback.onContactsListLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        databaseCallback.onNoContacts()
                    }
                })
    }

    fun getContactById(databaseCallback: DatabaseCallback, contactUid:String) {
        MainApplication.db.contactDAO().loadContactByIds(contactUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Contact>() {
                    override fun onSuccess(t: com.macgavrina.co_accounting.room.Contact) {
                        databaseCallback.onContactLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }


    fun addContact(databaseCallback: DatabaseCallback, contact: Contact) {
        Completable.fromAction {
            MainApplication.db.contactDAO().insertContact(contact)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onContactAdded()
                        //syncDataUpload()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }


    fun updateContact(databaseCallback: DatabaseCallback, contact: Contact) {
        Completable.fromAction(object : Action {
            @Throws(Exception::class)
            override fun run() {
                MainApplication.db.contactDAO().updateContact(contact)
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                databaseCallback.onContactUpdated()
                //syncDataUpload()
            }

            override fun onError(e: Throwable) {
                databaseCallback.onDatabaseError()
            }
        })
    }

    fun deleteContact(databaseCallback: DatabaseCallback, contact:Contact) {
        Completable.fromAction {
            MainApplication.db.contactDAO().deleteContact(contact) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                databaseCallback.onContactDeleted()
                //syncDataUpload()
            }

            override fun onError(e: Throwable) {
                databaseCallback.onDatabaseError()
            }
        })
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

    //ToDo REFACT передавать userToken не в методе а в конструкторе ContactsProvider
    fun syncDataDownload(userToken: String) {
        // Pass the settings flags by inserting them in a bundle
        SyncService.syncData(false, true, userToken)
    }

    fun syncDataUpload(userToken: String) {
        // Pass the settings flags by inserting them in a bundle
        SyncService.syncData(true, false, userToken)
    }

    interface DatabaseCallback {

        fun onContactUpdated() {
            Log.d("contact is updated")
        }

        fun onDatabaseError()

        fun onContactDeleted() {
            Log.d("contact is deleted")
        }

        fun onContactAdded() {
            Log.d("contact id added")
        }

        fun onContactLoaded(contact: Contact) {
            Log.d("contact is loaded")
        }

        fun onContactsListLoaded(contactsList: List<Contact>) {
            Log.d("contacts list is loaded")
        }

        fun onNoContacts() {
            Log.d("there is no contacts in DB")
        }
    }
}