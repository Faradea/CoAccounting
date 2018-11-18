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


    fun addExpense(databaseCallback: DatabaseCallback, expense: Expense) {
        MainApplication.db.expenseDAO().insertExpense(expense)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Long>() {
                    override fun onSuccess(uid: Long) {
                        databaseCallback.onExpenseAdded(uid)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }

    interface DatabaseCallback {

        fun onDatabaseError()

        fun onExpenseAdded(uid: Long) {
            Log.d("expense is added, uid = $uid")
        }

        fun onExpenseListLoaded(debtList: List<Expense>) {
            Log.d("expense list is loaded")
        }
    }
}