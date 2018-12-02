package com.macgavrina.co_accounting.providers

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.ReceiverWithAmountForDB
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ReceiverForAmountProvider {

    fun getAll(databaseCallback: DatabaseCallback) {
        MainApplication.db.receiverWithAmountForDBDAO().getAll
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<ReceiverWithAmountForDB>>() {
                    override fun onSuccess(t: List<ReceiverWithAmountForDB>) {
                        Log.d("success")
                        databaseCallback.onReceiversWithAmountListLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }

    fun getReceiversWithAmountForExpense(databaseCallback: DatabaseCallback, expenseId: String) {
        Log.d("getting receiverWithAmount for expenseId = $expenseId")
        MainApplication.db.receiverWithAmountForDBDAO().getReceiversWithAmountForExpense(expenseId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<ReceiverWithAmountForDB>>() {
                    override fun onSuccess(t: List<ReceiverWithAmountForDB>) {
                        Log.d("success")
                        databaseCallback.onReceiversWithAmountForExpenseListLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }


    fun addReceiverWithAmount(databaseCallback: DatabaseCallback, receiverForAmountForDB: ReceiverWithAmountForDB) {
        Completable.fromAction {
            MainApplication.db.receiverWithAmountForDBDAO().insertReceiverWithAmount(receiverForAmountForDB)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onReceiverWithAmountAdded()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }

    fun addReceiverWithAmountList(databaseCallback: DatabaseCallback, receiverForAmountForDBList: List<ReceiverWithAmountForDB>) {
        Completable.fromAction {


            MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiverForAmountForDBList.toTypedArray())
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onReceiverWithAmountListAdded()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }

    fun deleteReceiversWithAmountForExpense(databaseCallback: DatabaseCallback, expenseId: String) {
        Completable.fromAction {


            MainApplication.db.receiverWithAmountForDBDAO().deleteReceiversWithAmountForExpense(expenseId)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onReceiversWithAmountListForExpensesDeleted()
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }

    interface DatabaseCallback {

        fun onDatabaseError()

        fun onReceiverWithAmountListAdded() {
            Log.d("receiver with amount is added")
        }

        fun onReceiverWithAmountAdded() {
            Log.d("receiver with amount is added")
        }

        fun onReceiversWithAmountListLoaded(receiversWithAmountList: List<ReceiverWithAmountForDB>) {
            Log.d("receivers with amount list is loaded")
        }

        fun onReceiversWithAmountForExpenseListLoaded(receiversWithAmountList: List<ReceiverWithAmountForDB>) {
            Log.d("receivers with amount list for expense is loaded, list size = ${receiversWithAmountList.size}")
        }

        fun onReceiversWithAmountListForExpensesDeleted() {
            Log.d("all receivers with amount for expense are deleted")
        }
    }
}