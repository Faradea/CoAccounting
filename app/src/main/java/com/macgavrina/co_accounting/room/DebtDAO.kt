package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import com.macgavrina.co_accounting.support.STATUS_DELETED
import com.macgavrina.co_accounting.support.STATUS_DRAFT
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface DebtDAO {
    @Query("SELECT * FROM debt WHERE status IN (:status) ORDER BY datetime DESC")
    fun getAll(status: String): Maybe<List<Debt>>

    @Query("SELECT * FROM debt WHERE status IN (:status) AND tripId IN (:tripId) ORDER BY datetime DESC")
    fun getAllForTrip(status: String, tripId: Int): Maybe<List<Debt>>

    @Query("select debt.*, currency.symbol as currencySymbol from debt INNER JOIN trip ON Debt.tripId = Trip.uid AND Trip.isCurrent = 1 AND Trip.status = $STATUS_ACTIVE AND Debt.status = $STATUS_ACTIVE INNER JOIN currency ON debt.currencyId = currency.uid")
    fun getDebtsForCurrentTrip(): LiveData<List<Debt>>

    @Query("select debt.*, SenderWithAmount.contactId as senderId FROM debt " +
            "LEFT JOIN SenderWithAmount ON SenderWithAmount.debtId = debt.uid AND SenderWithAmount.status = $STATUS_ACTIVE " +
            "WHERE debt.uid = :debtId")
    fun getDebtByIds(debtId: Int): LiveData<Debt>

    @Query("select debt.*, SenderWithAmount.contactId as senderId FROM debt " +
            "LEFT JOIN SenderWithAmount ON SenderWithAmount.debtId = debt.uid AND SenderWithAmount.status = $STATUS_ACTIVE " +
            "WHERE debt.uid = :debtId")
    fun getDebtByIdRx(debtId: Int): Maybe<Debt>

    @Query("select (debtAmount - expensesAmount) FROM " +
            "( " +
            "(select debt.spentAmount as debtAmount from debt where debt.uid = :debtId) " +
            "JOIN " +
            "(select sum(expense.totalAmount) as expensesAmount from expense " +
            "where expense.debtId = :debtId AND expense.status = $STATUS_ACTIVE ) " +
            ")")
    fun getDebtRemains(debtId: Int): Maybe<Double>

    @Query("select debt.*, SenderWithAmount.contactId as senderId FROM debt " +
            "LEFT JOIN SenderWithAmount ON SenderWithAmount.debtId = debt.uid AND SenderWithAmount.status = $STATUS_ACTIVE " +
            "WHERE debt.status = $STATUS_DRAFT")
    fun getDebtDraft(): LiveData<Debt>

    @Query("select debt.*, SenderWithAmount.contactId as senderId FROM debt " +
            "LEFT JOIN SenderWithAmount ON SenderWithAmount.debtId = debt.uid AND SenderWithAmount.status = $STATUS_ACTIVE " +
            "WHERE debt.status = $STATUS_DRAFT")
    fun getDebtDraftRx(): Maybe<Debt>

    @Query ("select COUNT(*) from SenderWithAmount " +
            "LEFT JOIN debt ON debt.uid = SenderWithAmount.debtId AND debt.status = $STATUS_ACTIVE " +
            "WHERE SenderWithAmount.contactId = :contactId AND SenderWithAmount.status = $STATUS_ACTIVE")
    fun checkDebtsForContact(contactId: String): Maybe<Int>

//    @Query ("SELECT debt.* FROM debt INNER JOIN trip ON debt.tripId = trip.uid WHERE debt.senderId IN (:contactId) AND debt.status IN (:contactStatus) AND trip.isCurrent = 1 AND trip.status = $STATUS_ACTIVE")
//    fun checkDebtsForContactAndCurrentTrip(contactId: String, contactStatus: String): Maybe<List<Debt>>

    @Insert
    fun insertDebt(debt: Debt)

    @Query("UPDATE debt SET status = $STATUS_DELETED WHERE uid IN (:debtId)")
    fun deleteDebt(debtId: Int)

    @Update
    fun updateDebt(debt: Debt)

    //For DBBrowser:
//    SELECT contactAlias as contactAlias, contactId, SUM(amount) as totalAmount, currencySymbol
//    FROM (select Contact.alias as contactAlias, Debt.senderId as contactId, Debt.spentAmount as amount,
//    currency.uid as currencyId, currency.symbol as currencySymbol from Debt
//    INNER JOIN Contact ON Contact.uid = Debt.senderId
//    INNER JOIN trip ON Debt.tripId = Trip.uid
//    INNER JOIN currency ON Debt.currencyId = Currency.uid
//    WHERE Trip.isCurrent = 1 AND trip.status = "active" AND debt.status = "active" AND debt.isCorrect = 1
//    UNION ALL
//    select Contact.alias as contactAlias, ReceiverWithAmountForDB.contactId as contactId, -SUM(ReceiverWithAmountForDB.amount) as amount,
//    currency.uid as currencyId, currency.symbol as currencySymbol
//    FROM ReceiverWithAmountForDB
//    LEFT JOIN Expense ON ReceiverWithAmountForDB.expenseId = Expense.uid
//    INNER JOIN Contact ON ReceiverWithAmountForDB.contactId = Contact.uid
//    INNER JOIN debt ON debt.uid = Expense.debtId
//    INNER JOIN trip ON debt.tripId = Trip.uid
//    INNER JOIN currency ON Debt.currencyId = Currency.uid
//    WHERE Trip.isCurrent = 1 AND trip.status = "active" AND debt.status = "active" AND debt.isCorrect = 1
//    GROUP BY ReceiverWithAmountForDB.contactId, Debt.currencyId)
//    GROUP BY contactAlias, currencyId
    @Query("SELECT alias as contactAlias, contactId, SUM(amount) as totalAmount, currencySymbol " +
            "FROM (select Contact.alias, SenderWithAmount.contactId as contactId, Debt.spentAmount as amount, " +
            "currency.uid as currencyId, currency.symbol as currencySymbol from Debt " +
            "            INNER JOIN SenderWithAmount ON debt.uid = SenderWithAmount.debtId AND SenderWithAmount.status = $STATUS_ACTIVE " +
            "            INNER JOIN Contact ON Contact.uid = SenderWithAmount.contactId" +
            "            INNER JOIN trip ON Debt.tripId = Trip.uid " +
            " INNER JOIN currency ON Debt.currencyId = Currency.uid " +
            "            WHERE Trip.isCurrent = 1 AND trip.status = $STATUS_ACTIVE AND debt.status = $STATUS_ACTIVE AND debt.isCorrect = 1 " +
            "            UNION ALL " +
            "            select Contact.alias, ReceiverWithAmountForDB.contactId, -SUM(ReceiverWithAmountForDB.amount) as amount, " +
            " currency.uid as currencyId, currency.symbol as currencySymbol  from ReceiverWithAmountForDB " +
            "            LEFT JOIN Expense ON ReceiverWithAmountForDB.expenseId = Expense.uid AND expense.status = $STATUS_ACTIVE " +
            "            INNER JOIN Contact ON ReceiverWithAmountForDB.contactId = Contact.uid " +
            "            INNER JOIN debt ON debt.uid = Expense.debtId " +
            "            INNER JOIN trip ON debt.tripId = Trip.uid " +
            " INNER JOIN currency ON Debt.currencyId = Currency.uid " +
            "            WHERE Trip.isCurrent = 1 AND trip.status = $STATUS_ACTIVE AND debt.status = $STATUS_ACTIVE AND debt.isCorrect = 1 AND ReceiverWithAmountForDB.status = $STATUS_ACTIVE " +
            "            GROUP BY ReceiverWithAmountForDB.contactId, Debt.currencyId) " +
            "            GROUP BY contactId, currencyId")
    fun getAllCalculationsForCurrentTrip(): LiveData<List<Calculation>>

    @Query("UPDATE SenderWithAmount SET status = $STATUS_DELETED WHERE debtId = :debtId")
    fun deleteAllSendersWithAmountForDebt(debtId: Int): Completable

    @Insert
    fun addSenderWithAmountForDebt(senderWithAmount: SenderWithAmount): Completable
}