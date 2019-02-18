package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface DebtDAO {
    @Query("SELECT * FROM debt WHERE status IN (:status) ORDER BY datetime DESC")
    fun getAll(status: String): Maybe<List<Debt>>

    @Query("SELECT * FROM debt WHERE status IN (:status) AND tripId IN (:tripId) ORDER BY datetime DESC")
    fun getAllForTrip(status: String, tripId: Int): Maybe<List<Debt>>

    @Query("select debt.* from debt INNER JOIN trip ON Debt.tripId = Trip.uid AND Trip.isCurrent = 1 AND Debt.status = \"active\"")
    fun getDebtsForCurrentTrip(): LiveData<List<Debt>>

    @Query("SELECT * FROM debt WHERE uid IN (:debtId)")
    fun getDebtByIds(debtId: Int): LiveData<Debt>

    @Query("SELECT * FROM debt WHERE status = \"draft\"")
    fun getDebtDraft(): LiveData<Debt>

    //ToDo REFACT use count instead of Select all
    @Query ("SELECT * FROM debt WHERE senderId IN (:contactId) AND status IN (:contactStatus)")
    fun checkDebtsForContact(contactId: String, contactStatus: String): Maybe<List<Debt>>

    @Query ("SELECT debt.* FROM debt INNER JOIN trip ON debt.tripId = trip.uid WHERE debt.senderId IN (:contactId) AND debt.status IN (:contactStatus) AND trip.isCurrent = 1 AND trip.status = \"active\"")
    fun checkDebtsForContactAndCurrentTrip(contactId: String, contactStatus: String): Maybe<List<Debt>>

    @Insert
    fun insertDebt(debt: Debt)

    @Query("UPDATE debt SET status = \"deleted\" WHERE uid IN (:debtId)")
    fun deleteDebt(debtId: Int)

    @Update
    fun updateDebt(debt: Debt)

    @Query("SELECT alias as contactAlias, contactId, SUM(amount) as totalAmount " +
            "FROM (select Contact.alias, Debt.senderId as contactId, Debt.spentAmount as amount from Debt " +
            "INNER JOIN Contact ON Contact.uid = Debt.senderId " +
            "INNER JOIN trip ON Debt.tripId = Trip.uid " +
            "WHERE Trip.isCurrent = 1 AND trip.status = \"active\" AND debt.status = \"active\" " +
            "UNION ALL " +
            "select Contact.alias, ReceiverWithAmountForDB.contactId, -SUM(ReceiverWithAmountForDB.amount) as amount from ReceiverWithAmountForDB " +
            "LEFT JOIN Expense ON ReceiverWithAmountForDB.expenseId = Expense.uid " +
            "INNER JOIN Contact ON ReceiverWithAmountForDB.contactId = Contact.uid " +
            "INNER JOIN debt ON debt.uid = Expense.debtId " +
            "INNER JOIN trip ON debt.tripId = Trip.uid " +
            "WHERE Trip.isCurrent = 1 AND trip.status = \"active\" AND debt.status = \"active\" " +
            "GROUP BY ReceiverWithAmountForDB.contactId) " +
            "GROUP BY contactId")
    fun getAllCalculationsForCurrentTrip(): LiveData<List<Calculation>>
}