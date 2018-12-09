package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface DebtDAO {
    @Query("SELECT * FROM debt WHERE status IN (:status) ORDER BY datetime DESC")
    fun getAll(status: String): Maybe<List<Debt>>

    @Query("SELECT * FROM debt WHERE uid IN (:debtId)")
    fun getDebtByIds(debtId: String): Maybe<Debt>

    @Query("SELECT * FROM debt WHERE status IN (:status)")
    fun getDebtDraft(status: String): Maybe<Debt>

    //ToDo REFACT заменить на count
    @Query ("SELECT * FROM debt WHERE senderId IN (:contactId)")
    fun checkDebtsForContact(contactId: String): Maybe<List<Debt>>

    @Insert
    fun insertDebt(debt: Debt)

    @Delete
    fun deleteDebt(debt: Debt)

    @Delete
    fun deleteDebts(vararg debt: Debt)

    @Update
    fun updateDebt(debt: Debt)
}