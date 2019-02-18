package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Maybe

@Dao
interface ExpenseDAO {
    @get:Query("SELECT * FROM expense")
    val getAll: Maybe<List<Expense>>

//    @Query("SELECT * FROM expense WHERE uid IN (:expenseId)")
//    fun getExpenseByIds(expenseId: String): Maybe<Expense>

    @Query("SELECT uid FROM expense ORDER BY uid DESC LIMIT 1")
    fun getLastExpenseId(): Maybe<Int>

    @Query("SELECT expense.uid, expense.debtId, expense.expenseName, group_concat(contact.alias, :separator) as receiversList, expense.totalAmount FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.debtId IN (:debtId) GROUP BY receiverwithamountfordb.expenseId ORDER BY expense.uid")
    fun getExpensesForDebt(debtId: Int, separator: String): LiveData<List<Expense>>

    @Query("SELECT expense.uid, expense.debtId, expense.expenseName, group_concat(contact.alias, :separator) as receiversList, expense.totalAmount " +
            "FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "INNER JOIN debt ON Expense.debtId = debt.uid " +
            "WHERE debt.status = \"draft\" " +
            "GROUP BY receiverwithamountfordb.expenseId " +
            "ORDER BY expense.uid")
    fun getExpensesForDebtDraft(separator: String): LiveData<List<Expense>>

    @Query("DELETE FROM expense WHERE debtId IN (:debtId)")
    fun deleteExpensesForDebt(debtId: String)

    @Insert
    fun insertExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Update
    fun updateExpense(expense: Expense)

    @Query("SELECT expense.uid, expense.debtId, expense.expenseName, group_concat(contact.alias, \", \") as receiversList, expense.totalAmount FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid IN (:expenseId) GROUP BY receiverwithamountfordb.expenseId")
    fun getExpenseByIds(expenseId: String): Maybe<Expense>
}