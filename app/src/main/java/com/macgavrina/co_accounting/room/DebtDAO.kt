package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface DebtDAO {
    @Query("SELECT * FROM debt WHERE status IN (:status) ORDER BY datetime DESC")
    fun getAll(status: String): Maybe<List<Debt>>

    @Query("SELECT * FROM debt WHERE status IN (:status) AND tripId IN (:tripId) ORDER BY datetime DESC")
    fun getAllForTrip(status: String, tripId: Int): Maybe<List<Debt>>

    @Query("SELECT * FROM debt WHERE uid IN (:debtId)")
    fun getDebtByIds(debtId: String): Maybe<Debt>

    @Query("SELECT * FROM debt WHERE status IN (:status)")
    fun getDebtDraft(status: String): Maybe<Debt>

    //ToDo REFACT use count instead of Select all
    @Query ("SELECT * FROM debt WHERE senderId IN (:contactId) AND status IN (:contactStatus)")
    fun checkDebtsForContact(contactId: String, contactStatus: String): Maybe<List<Debt>>

    @Insert
    fun insertDebt(debt: Debt)

    @Query("UPDATE debt SET status = :status WHERE uid IN (:debtId)")
    fun deleteDebt(debtId: String, status: String)

    @Update
    fun updateDebt(debt: Debt)
}