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

    @Query("select debt.*, currency.symbol as currencySymbol from debt INNER JOIN trip ON Debt.tripId = Trip.uid AND Trip.isCurrent = 1 AND Debt.status = \"active\" INNER JOIN currency ON debt.currencyId = currency.uid")
    fun getDebtsForCurrentTrip(): LiveData<List<Debt>>

    @Query("SELECT * FROM debt WHERE uid IN (:debtId)")
    fun getDebtByIds(debtId: Int): LiveData<Debt>

    @Query("SELECT * FROM debt WHERE uid IN (:debtId)")
    fun getDebtByIdRx(debtId: Int): Maybe<Debt>

    @Query("select (debtAmount - expensesAmount) FROM " +
            "( " +
            "(select debt.spentAmount as debtAmount from debt where debt.uid = :debtId) " +
            "JOIN " +
            "(select sum(expense.totalAmount) as expensesAmount from expense " +
            "where expense.debtId = :debtId) " +
            ")")
    fun getDebtRemains(debtId: Int): Maybe<Double>

    @Query("SELECT * FROM debt WHERE status = \"draft\"")
    fun getDebtDraft(): LiveData<Debt>

    @Query("SELECT * FROM debt WHERE status = \"draft\"")
    fun getDebtDraftRx(): Maybe<Debt>

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

    @Query("SELECT alias as contactAlias, contactId, SUM(amount) as totalAmount, currencySymbol " +
            "FROM (select Contact.alias, Debt.senderId as contactId, Debt.spentAmount as amount, " +
            "currency.uid as currencyId, currency.symbol as currencySymbol from Debt " +
            "            INNER JOIN Contact ON Contact.uid = Debt.senderId " +
            "            INNER JOIN trip ON Debt.tripId = Trip.uid " +
            " INNER JOIN currency ON Debt.currencyId = Currency.uid " +
            "            WHERE Trip.isCurrent = 1 AND trip.status = \"active\" AND debt.status = \"active\" AND debt.isCorrect = 1 " +
            "            UNION ALL " +
            "            select Contact.alias, ReceiverWithAmountForDB.contactId, -SUM(ReceiverWithAmountForDB.amount) as amount, " +
            " currency.uid as currencyId, currency.symbol as currencySymbol  from ReceiverWithAmountForDB " +
            "            LEFT JOIN Expense ON ReceiverWithAmountForDB.expenseId = Expense.uid " +
            "            INNER JOIN Contact ON ReceiverWithAmountForDB.contactId = Contact.uid " +
            "            INNER JOIN debt ON debt.uid = Expense.debtId " +
            "            INNER JOIN trip ON debt.tripId = Trip.uid " +
            " INNER JOIN currency ON Debt.currencyId = Currency.uid " +
            "            WHERE Trip.isCurrent = 1 AND trip.status = \"active\" AND debt.status = \"active\" AND debt.isCorrect = 1 " +
            "            GROUP BY ReceiverWithAmountForDB.contactId, Debt.currencyId) " +
            "            GROUP BY contactId, currencyId")
    fun getAllCalculationsForCurrentTrip(): LiveData<List<Calculation>>
}