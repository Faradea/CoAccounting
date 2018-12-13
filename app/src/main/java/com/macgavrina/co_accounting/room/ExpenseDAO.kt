package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe

@Dao
interface ExpenseDAO {
    @get:Query("SELECT * FROM expense")
    val getAll: Maybe<List<Expense>>

    @Query("SELECT * FROM expense WHERE uid IN (:expenseId)")
    fun getExpenseByIds(expenseId: String): Maybe<Expense>

    @Query("SELECT uid FROM expense ORDER BY uid DESC LIMIT 1")
    fun getLastExpenseId(): Maybe<Int>

    @Query("SELECT * FROM expense WHERE debtId IN (:debtId) ORDER BY uid")
    fun getExpensesForDebt(debtId: String): Maybe<List<Expense>>

    @Query("DELETE FROM expense WHERE debtId IN (:debtId)")
    fun deleteExpensesForDebt(debtId: String)

    @Insert
    fun insertExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Update
    fun updateExpense(expense: Expense)
}