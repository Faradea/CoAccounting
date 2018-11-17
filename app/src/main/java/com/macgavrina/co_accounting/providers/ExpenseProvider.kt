package com.macgavrina.co_accounting.providers

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Expense
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class ExpenseProvider() {

    fun getAll(databaseCallback: DatabaseCallback) {
        MainApplication.db.expenseDAO().getAll
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Expense>>() {
                    override fun onSuccess(t: List<Expense>) {
                        Log.d("success")
                        databaseCallback.onExpenseListLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }

//    fun getDebtById(databaseCallback: DatabaseCallback, debtUid:String) {
//        MainApplication.db.debtDAO().loadDebtByIds(debtUid)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : DisposableMaybeObserver<Contact>() {
//                    override fun onSuccess(t: com.macgavrina.co_accounting.room.Contact) {
//                        databaseCallback.onContactLoaded(t)
//                    }
//
//                    override fun onError(e: Throwable) {
//                        Log.d(e.toString())
//                    }
//
//                    override fun onComplete() {
//                        Log.d("nothing")
//                    }
//                })
//    }


    fun addExpense(databaseCallback: DatabaseCallback, expense: Expense) {
        Completable.fromAction {
            MainApplication.db.expenseDAO().insertExpense(expense)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        databaseCallback.onExpenseAdded(uid)
                    }

                    override fun onError(e: Throwable) {
                        databaseCallback.onDatabaseError()
                    }
                })
    }

    interface DatabaseCallback {

        fun onDatabaseError()

        fun onExpenseAdded(uid: Long) {
            Log.d("expense is added")
        }

        fun onExpenseListLoaded(debtList: List<Expense>) {
            Log.d("expense list is loaded")
        }
    }
}