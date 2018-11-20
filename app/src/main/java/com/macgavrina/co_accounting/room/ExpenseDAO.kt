package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe

@Dao
interface ExpenseDAO {
    @get:Query("SELECT * FROM expense")
    val getAll: Maybe<List<Expense>>

    @Query("SELECT * FROM expense WHERE uid IN (:expenseId)")
    fun loadExpenseByIds(expenseId: String): Maybe<Expense>

    @Query("SELECT uid FROM expense ORDER BY uid LIMIT 1")
    fun getLastExpenseId(): Maybe<Int>

    @Query("SELECT * FROM expense WHERE debtId IN (:debtId) ORDER BY uid")
    fun getExpensesForDebt(debtId: String): Maybe<List<Expense>>

    @Insert
    fun insertExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Update
    fun updateExpense(expense: Expense)
}