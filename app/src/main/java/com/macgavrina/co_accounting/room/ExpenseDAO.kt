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
interface ExpenseDAO {
    @get:Query("SELECT * FROM expense WHERE expense.status = $STATUS_ACTIVE")
    val getAll: Maybe<List<Expense>>

//    @Query("SELECT * FROM expense WHERE uid IN (:expenseId)")
//    fun getExpenseByIds(expenseId: String): Maybe<Expense>

    @Query("SELECT uid FROM expense ORDER BY uid DESC LIMIT 1")
    fun getLastExpenseId(): Maybe<Int>

    @Query("SELECT expense.uid, expense.debtId, expense.comment, group_concat(contact.alias, :separator) as receiversList, expense.totalAmount, expense.isForExpertMode, expense.status " +
            "FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "WHERE expense.debtId IN (:debtId) AND expense.isForExpertMode = :isExpertMode AND expense.status = $STATUS_ACTIVE " +
            "AND receiverwithamountfordb.status = $STATUS_ACTIVE GROUP BY receiverwithamountfordb.expenseId ORDER BY expense.uid")
    fun getExpensesForDebt(debtId: Int, separator: String, isExpertMode: Boolean): LiveData<List<Expense>>

    @Query("SELECT contact.* " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "WHERE expense.uid = :expenseId AND receiverwithamountfordb.status = $STATUS_ACTIVE")
    fun getSelectedContactsForExpenseId(expenseId: Int): LiveData<List<Contact>>

    @Query("SELECT contact.* " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "WHERE expense.uid = :expenseId AND receiverwithamountfordb.status = $STATUS_ACTIVE")
    fun getSelectedContactsForExpenseIdRx(expenseId: Int): Single<List<Contact>>

    @Query("SELECT contact.* " +
            " from contact " +
            "INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE " +
            "INNER JOIN trip ON contacttotriprelation.tripId = trip.uid " +
            "WHERE contact.status = $STATUS_ACTIVE AND trip.isCurrent = 1 " +
            "AND contact.uid NOT IN " +
            "(SELECT contact.uid " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid AND receiverwithamountfordb.status = $STATUS_ACTIVE " +
            " INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid = :expenseId )")
    fun getNotSelectedContactsForExpenseId(expenseId: Int): LiveData<List<Contact>>

    @Query("SELECT contact.* " +
            " from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid AND contacttotriprelation.status = $STATUS_ACTIVE " +
            " INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = $STATUS_ACTIVE AND trip.isCurrent = 1 " +
            " AND contact.uid NOT IN " +
            "(SELECT contact.uid " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid AND receiverwithamountfordb.status = $STATUS_ACTIVE " +
            " INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid = :expenseId )")
    fun getNotSelectedContactsForExpenseIdRx(expenseId: Int): Single<List<Contact>>


    @Query("SELECT expense.uid, expense.debtId, expense.comment, expense.isForExpertMode, group_concat(contact.alias, :separator) as receiversList, expense.totalAmount, expense.status " +
            "FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "INNER JOIN debt ON Expense.debtId = debt.uid " +
            "WHERE debt.status = $STATUS_DRAFT AND expense.isForExpertMode = :isExpertMode AND expense.status = $STATUS_ACTIVE " +
            "AND receiverwithamountfordb.status = $STATUS_ACTIVE " +
            "GROUP BY receiverwithamountfordb.expenseId " +
            "ORDER BY expense.uid")
    fun getExpensesForDebtDraft(separator: String, isExpertMode: Boolean): LiveData<List<Expense>>

    @Query("SELECT SUM(totalAmount) from expense WHERE debtId = :debtId AND expense.status = $STATUS_ACTIVE")
    fun getExpensesTotalAmountForDebt(debtId: Int): Single<Double>

    @Query("UPDATE expense SET status = $STATUS_DELETED WHERE debtId IN (:debtId)")
    fun deleteExpensesForDebt(debtId: String)

    @Query("UPDATE expense SET status = $STATUS_DELETED WHERE debtId IN (:debtId) AND isForExpertMode = 0")
    fun deleteExpensesForDebtAndSimpleMode(debtId: Int): Completable

    @Insert
    fun insertExpense(expense: Expense)

    @Query("UPDATE expense SET status = $STATUS_DELETED WHERE uid = :expenseId")
    fun deleteExpense(expenseId: Int)

    @Update
    fun updateExpense(expense: Expense)

    @Query("SELECT expense.uid, expense.debtId, expense.isForExpertMode, expense.comment, group_concat(contact.alias, \", \") as receiversList, expense.totalAmount, expense.status " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "WHERE expense.uid IN (:expenseId) AND receiverwithamountfordb.status = $STATUS_ACTIVE " +
            "GROUP BY receiverwithamountfordb.expenseId")
    fun getExpenseByIds(expenseId: Int): Maybe<Expense>

    @Query("select SUM(totalAmount) from expense where debtId = :debtId AND isForExpertMode = 1 AND expense.status = $STATUS_ACTIVE")
    fun getExpensesSumForDebtAndExpertMode(debtId: Int): LiveData<Double>
}