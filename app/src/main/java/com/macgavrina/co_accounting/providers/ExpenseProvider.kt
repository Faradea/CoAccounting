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

    fun getExpensesForDebt(databaseCallback: DatabaseCallback, debtId: String) {
        MainApplication.db.expenseDAO().getExpensesForDebt(debtId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Expense>>() {
                    override fun onSuccess(t: List<Expense>) {
                        databaseCallback.onExpensesForDebtListLoaded(t)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                        databaseCallback.onNoExpensesForDebt()
                    }
                })
    }


    //ToDo Use  Maybe<Long> in DAO after Room bugfixing
//    fun addExpense(databaseCallback: DatabaseCallback, expense: Expense) {
//        MainApplication.db.expenseDAO().insertExpense(expense)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : DisposableMaybeObserver<Long>() {
//                    override fun onSuccess(uid: Long) {
//                        databaseCallback.onExpenseAdded(uid)
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

        fun getLastExpenseId(databaseCallback: DatabaseCallback) {
        MainApplication.db.expenseDAO().getLastExpenseId()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Int>() {
                    override fun onSuccess(uid: Int) {
                        databaseCallback.onGetLastExpenseId(uid)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        Log.d("nothing")
                    }
                })
    }

    fun getExpenseById(databaseCallback: DatabaseCallback, expenseId: Int) {
        MainApplication.db.expenseDAO().getExpenseByIds(expenseId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<Expense>() {
                    override fun onSuccess(expense: Expense) {
                        databaseCallback.onExpenseByIdLoaded(expense)
                    }

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        databaseCallback.onNoExpenseWithRequestedId()
                    }
                })
    }

    fun addExpense(databaseCallback: DatabaseCallback, expense: Expense) {
        Completable.fromAction {
            MainApplication.db.expenseDAO().insertExpense(expense)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {

                        override fun onSubscribe(d: Disposable) {}

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                            databaseCallback.onExpenseAdded()
                        }
                    })
        }

    fun deleteExpense(databaseCallback: DatabaseCallback, expense: Expense) {
        Completable.fromAction {
            MainApplication.db.expenseDAO().deleteExpense(expense)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {

                    override fun onSubscribe(d: Disposable) {}

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        databaseCallback.onExpenseDeleted()
                    }
                })
    }

    fun updateExpense(databaseCallback: DatabaseCallback, expense: Expense) {
        Completable.fromAction {
            MainApplication.db.expenseDAO().updateExpense(expense)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CompletableObserver {

                    override fun onSubscribe(d: Disposable) {}

                    override fun onError(e: Throwable) {
                        Log.d(e.toString())
                    }

                    override fun onComplete() {
                        databaseCallback.onExpenseUpdated()
                    }
                })
    }


    interface DatabaseCallback {

        fun onDatabaseError()

        fun onExpenseAdded() {
            Log.d("expense is added")
        }

        fun onExpenseListLoaded(debtList: List<Expense>) {
            Log.d("expense list is loaded")
        }

        fun onGetLastExpenseId(uid: Int) {
            Log.d("last expense id = $uid")
        }

        fun onExpensesForDebtListLoaded(expenseList: List<Expense>) {
            Log.d("expenses for debt list is loaded")
        }

        fun onNoExpensesForDebt() {
            Log.d("there is no expenses for debt")
        }

        fun onExpenseByIdLoaded(expense: Expense) {
            Log.d("expense is loaded")
        }

        fun onNoExpenseWithRequestedId() {
            Log.d("no expenses for requested id")
        }

        fun onExpenseDeleted() {
            Log.d("expense is deleted")
        }

        fun onExpenseUpdated() {
            Log.d("expense is updated")
        }
    }
}