package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Expense
import com.macgavrina.co_accounting.room.ExpenseDAO

class ExpenseRepository {

    private var expenseDao: ExpenseDAO = MainApplication.db.expenseDAO()

    init {
    }

    fun getAllExpensesForDebt(debtId: Int): LiveData<List<Expense>> {
        if (debtId != -1) {
            return expenseDao.getExpensesForDebt(debtId, ", ")
        } else {
            return expenseDao.getExpensesForDebtDraft(", ")
        }
    }

    fun getReceiversForOnlyOneExpenseForDebt(debtId: Int): LiveData<List<Contact>> {
        return expenseDao.getReceiversForOnlyOneExpenseForDebt(debtId)
    }

//    fun getDebtById(debtId: Int): LiveData<Debt> {
//        if (debtId == -1) {
//            return debtDao.getDebtDraft()
//        } else {
//            return debtDao.getDebtByIds(debtId)
//        }
//    }

}