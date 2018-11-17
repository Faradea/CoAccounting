package com.macgavrina.co_accounting.room

import android.arch.persistence.room.*
import io.reactivex.Maybe

@Dao
interface ExpenseDAO {
    @get:Query("SELECT * FROM expense")
    val getAll: Maybe<List<Expense>>

    @Query("SELECT * FROM expense WHERE uid IN (:expenseId)")
    fun loadExpenseByIds(expenseId: String): Maybe<Expense>

    @Insert
    fun insertExpense(expense: Expense): Maybe<Long>

    @Delete
    fun deleteExpense(expense: Expense)

    @Update
    fun updateExpense(expense: Expense)
}