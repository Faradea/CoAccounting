package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.*
import com.macgavrina.co_accounting.support.MoneyFormatter
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

class ExpenseRepository {

    private var expenseDao: ExpenseDAO = MainApplication.db.expenseDAO()
    private var receiverWithAmountForDBDAO: ReceiverWithAmountForDBDAO = MainApplication.db.receiverWithAmountForDBDAO()

    init {
    }

    fun getExpenseByIdRx(expenseId: Int): Maybe<Expense> {
        return expenseDao.getExpenseByIds(expenseId)
    }

    fun getAllExpensesForDebtSimpleMode(debtId: Int): LiveData<List<Expense>> {
        if (debtId != -1) {
            return expenseDao.getExpensesForDebt(debtId, ", ", false)
        } else {
            return expenseDao.getExpensesForDebtDraft(", ", false)
        }
    }

    fun getAllExpensesForDebtExpertMode(debtId: Int): LiveData<List<Expense>> {
        if (debtId != -1) {
            return expenseDao.getExpensesForDebt(debtId, ", ", true)
        } else {
            return expenseDao.getExpensesForDebtDraft(", ", true)
        }
    }

    fun getExpensesTotalAmountForDebt(debtId: Int): Single<Double> {
        return expenseDao.getExpensesTotalAmountForDebt(debtId)
    }

    fun getSelectedContactsForExpense(expenseId: Int): LiveData<List<Contact>> {
        return expenseDao.getSelectedContactsForExpenseId(expenseId)
    }

    fun getSelectedContactsForExpenseRx(expenseId: Int): Single<List<Contact>> {
        return expenseDao.getSelectedContactsForExpenseIdRx(expenseId)
    }

    fun getNotSelectedContactsForExpense(expenseId: Int): LiveData<List<Contact>> {
        return expenseDao.getNotSelectedContactsForExpenseId(expenseId)
    }

    fun getNotSelectedContactsForExpenseRx(expenseId: Int): Single<List<Contact>> {
        return expenseDao.getNotSelectedContactsForExpenseIdRx(expenseId)
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
        expense.totalAmount = MoneyFormatter.justRound(expense.totalAmount)
        Log.d("Expense after rounding = $expense")
        return Completable.fromAction {
            expenseDao.updateExpense(expense)
        }
    }

    fun deleteAllReceiverWithAmountForExpense(expenseId: Int): Completable {
        return receiverWithAmountForDBDAO.deleteReceiversWithAmountForExpenseWithoutResult(expenseId.toString())
    }

    fun addReceiversWithAmountList(receiversWithAmountList: List<ReceiverWithAmountForDB>): Completable {

        val receiversWithAmountListRounded = mutableListOf<ReceiverWithAmountForDB>()
        receiversWithAmountList.forEach { receiverWithAmount ->
            receiverWithAmount.amount = MoneyFormatter.justRound(receiverWithAmount.amount)
            receiversWithAmountListRounded.add(receiverWithAmount)
        }

        Log.d("ReceiverWithAmount list after rounding = $receiversWithAmountListRounded")

        return Completable.fromAction {
            MainApplication.db.receiverWithAmountForDBDAO().insertAll(*receiversWithAmountListRounded!!.toTypedArray())
        }
    }

    fun deleteAllExpensesForDebt(debtId: Int): Completable {
        return Completable.fromAction {
            expenseDao.deleteExpensesForDebt(debtId.toString())
        }
    }

    fun deleteAllExpensesForDebtAndSimpleMode(debtId: Int): Completable {
        return expenseDao.deleteExpensesForDebtAndSimpleMode(debtId)
    }

    fun getExpensesSumForDebtAndExpertMode(debtId: Int): LiveData<Double> {
        return expenseDao.getExpensesSumForDebtAndExpertMode(debtId)
    }



//    fun getDebtById(debtId: Int): LiveData<Debt> {
//        if (debtId == -1) {
//            return debtDao.getDebtDraft()
//        } else {
//            return debtDao.getDebtByIds(debtId)
//        }
//    }

}