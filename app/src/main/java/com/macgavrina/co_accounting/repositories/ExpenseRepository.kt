package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

class ExpenseRepository {

    private var expenseDao: ExpenseDAO = MainApplication.db.expenseDAO()
    private var receiverWithAmountForDBDAO: ReceiverWithAmountForDBDAO = MainApplication.db.receiverWithAmountForDBDAO()

    init {
    }

    fun getAllExpensesForDebt(debtId: Int): LiveData<List<Expense>> {
        if (debtId != -1) {
            return expenseDao.getExpensesForDebt(debtId, ", ")
        } else {
            return expenseDao.getExpensesForDebtDraft(", ")
        }
    }

    fun getSelectedContactsForExpense(expenseId: Int): LiveData<List<Contact>> {
        return expenseDao.getSelectedContactsForExpenseId(expenseId)
    }

    fun getNotSelectedContactsForExpense(expenseId: Int): LiveData<List<Contact>> {
        return expenseDao.getNotSelectedContactsForExpenseId(expenseId)
    }

    fun insertNewExpense(expense: Expense): Completable {
        return Completable.fromAction {
            expenseDao.insertExpense(expense)
        }
    }

    fun getLastAddedExpenseId(): Maybe<Int> {
        return expenseDao.getLastExpenseId()
    }

    fun updateExpense(expense: Expense): Completable {
        return Completable.fromAction {
            expenseDao.updateExpense(expense)
        }
    }

    fun deleteAllReceiverWithAmountForExpense(expenseId: Int): Completable {
        return receiverWithAmountForDBDAO.deleteReceiversWithAmountForExpense(expenseId.toString())
    }

    fun addReceiversWithAmountList(receiversWithAmountList: List<ReceiverWithAmountForDB>): Completable {
        return Completable.fromAction {
            MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiversWithAmountList!!.toTypedArray())
        }
    }



//    fun getDebtById(debtId: Int): LiveData<Debt> {
//        if (debtId == -1) {
//            return debtDao.getDebtDraft()
//        } else {
//            return debtDao.getDebtByIds(debtId)
//        }
//    }

}