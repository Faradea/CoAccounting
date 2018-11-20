package com.macgavrina.co_accounting.providers

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class DebtsProvider() {

    //ToDo подумать насчет ContentProvider

    //ToDo сделать singleton (см. пример в SyncService)

    fun getAll(databaseCallback: DatabaseCallback) {
        Log.d("get all debts")
        MainApplication.db.debtDAO().getAll
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onSuccess(t: List<Debt>) {
                        Log.d("success")
                        databaseCallback.onDebtsListLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }

    fun getDebtDraft(databaseCallback: DatabaseCallback) {
        MainApplication.db.debtDAO().getDebtDraft("draft")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Debt>() {
                    override fun onSuccess(t: Debt) {
                        databaseCallback.onDebtDraftLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        databaseCallback.onNoDebtDraftExist()
                    }
                })
    }


    fun addDebt(databaseCallback: DatabaseCallback, debt: Debt) {
        Completable.fromAction {
            MainApplication.db.debtDAO().insertDebt(debt)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onDebtAdded()
                        //syncDataUpload()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }

    fun addDebtDraft(databaseCallback: DatabaseCallback) {
        Completable.fromAction {
            val debt = Debt()
            debt.status = "draft"
            MainApplication.db.debtDAO().insertDebt(debt)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onDebtDraftAdded()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }


//    fun updateContact(databaseCallback: DatabaseCallback, contact: Contact) {
//        Completable.fromAction(object : Action {
//            @Throws(Exception::class)
//            override fun run() {
//                MainApplication.db.contactDAO().updateContact(contact)
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : CompletableObserver {
//            override fun onSubscribe(d: Disposable) {
//
//            }
//
//            override fun onComplete() {
//                databaseCallback.onContactUpdated()
//                //syncDataUpload()
//            }
//
//            override fun onError(e: Throwable) {
//                databaseCallback.onDatabaseError()
//            }
//        })
//    }

//    //ToDo сделать удаление через update
//    fun deleteContact(databaseCallback: DatabaseCallback, contact:Contact) {
//        Completable.fromAction {
//            MainApplication.db.contactDAO().deleteContact(contact) }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : CompletableObserver {
//                    override fun onSubscribe(d: Disposable) {
//
//                    }
//
//                    override fun onComplete() {
//                        databaseCallback.onContactDeleted()
//                        //syncDataUpload()
//                    }
//
//                    override fun onError(e: Throwable) {
//                        databaseCallback.onDatabaseError()
//                    }
//                })

//    //ToDo передавать userToken не в методе а в конструкторе ContactsProvider
//    fun syncDataDownload(userToken: String) {
//        // Pass the settings flags by inserting them in a bundle
//        SyncService.syncData(false, true, userToken)
//    }
//
//    fun syncDataUpload(userToken: String) {
//        // Pass the settings flags by inserting them in a bundle
//        SyncService.syncData(true, false, userToken)
//    }

    interface DatabaseCallback {

        fun onDebtUpdated() {
            Log.d("debt is updated")
        }

        fun onDatabaseError()

        fun onDebtDeleted() {
            Log.d("debt is deleted")
        }

        fun onDebtAdded() {
            Log.d("debt id added")
        }

        fun onDebtLoaded(debt: Debt) {
            Log.d("debt is loaded")
        }

        fun onDebtsListLoaded(debtList: List<Debt>) {
            Log.d("debt list is loaded")
        }

        fun onDebtDraftLoaded(debt: Debt) {
            Log.d("Debt draft is loaded, $debt")
        }

        fun onNoDebtDraftExist() {
            Log.d("debt draft doesn't exist")
        }

        fun onDebtDraftAdded() {
            Log.d("new empty debt draft is created")
        }
    }
}
