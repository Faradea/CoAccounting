package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe

@Dao
interface DebtDAO {
    @get:Query("SELECT * FROM debt")
    val getAll: Maybe<List<Debt>>

    @Query("SELECT * FROM debt WHERE uid IN (:debtId)")
    fun loadDebtByIds(debtId: String): Maybe<Debt>

    @Insert
    fun insertDebt(debt: Debt)

    @Delete
    fun deleteDebt(debt: Debt)

    @Delete
    fun deleteDebts(vararg debt: Debt)

    @Update
    fun updateDebt(debt: Debt)
}