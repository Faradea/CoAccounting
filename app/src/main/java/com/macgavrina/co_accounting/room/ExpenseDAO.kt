package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ExpenseDAO {
    @get:Query("SELECT * FROM expense")
    val getAll: Maybe<List<Expense>>

//    @Query("SELECT * FROM expense WHERE uid IN (:expenseId)")
//    fun getExpenseByIds(expenseId: String): Maybe<Expense>

    @Query("SELECT uid FROM expense ORDER BY uid DESC LIMIT 1")
    fun getLastExpenseId(): Maybe<Int>

    @Query("SELECT expense.uid, expense.debtId, expense.comment, group_concat(contact.alias, :separator) as receiversList, expense.totalAmount, expense.isForExpertMode FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.debtId IN (:debtId) AND expense.isForExpertMode = :isExpertMode GROUP BY receiverwithamountfordb.expenseId ORDER BY expense.uid")
    fun getExpensesForDebt(debtId: Int, separator: String, isExpertMode: Boolean): LiveData<List<Expense>>

    @Query("SELECT contact.* " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid = :expenseId")
    fun getSelectedContactsForExpenseId(expenseId: Int): LiveData<List<Contact>>

    @Query("SELECT contact.* " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid = :expenseId")
    fun getSelectedContactsForExpenseIdRx(expenseId: Int): Single<List<Contact>>

    @Query("SELECT contact.* " +
            " from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid " +
            " INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = \"active\" AND trip.isCurrent = 1 " +
            " AND contact.uid NOT IN " +
            "(SELECT contact.uid " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            " INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid = :expenseId)")
    fun getNotSelectedContactsForExpenseId(expenseId: Int): LiveData<List<Contact>>

    @Query("SELECT contact.* " +
            " from contact INNER JOIN contacttotriprelation ON contacttotriprelation.contactId = contact.uid " +
            " INNER JOIN trip ON contacttotriprelation.tripId = trip.uid WHERE contact.status = \"active\" AND trip.isCurrent = 1 " +
            " AND contact.uid NOT IN " +
            "(SELECT contact.uid " +
            "FROM expense " +
            "LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            " INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid = :expenseId)")
    fun getNotSelectedContactsForExpenseIdRx(expenseId: Int): Single<List<Contact>>


    @Query("SELECT expense.uid, expense.debtId, expense.comment, expense.isForExpertMode, group_concat(contact.alias, :separator) as receiversList, expense.totalAmount " +
            "FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid " +
            "INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid " +
            "INNER JOIN debt ON Expense.debtId = debt.uid " +
            "WHERE debt.status = \"draft\" AND expense.isForExpertMode = :isExpertMode " +
            "GROUP BY receiverwithamountfordb.expenseId " +
            "ORDER BY expense.uid")
    fun getExpensesForDebtDraft(separator: String, isExpertMode: Boolean): LiveData<List<Expense>>

    @Query("SELECT SUM(totalAmount) from expense WHERE debtId = :debtId")
    fun getExpensesTotalAmountForDebt(debtId: Int): Single<Double>

    @Query("DELETE FROM expense WHERE debtId IN (:debtId)")
    fun deleteExpensesForDebt(debtId: String)

    @Query("DELETE FROM expense WHERE debtId IN (:debtId) AND isForExpertMode = 0")
    fun deleteExpensesForDebtAndSimpleMode(debtId: Int): Completable

    @Insert
    fun insertExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Update
    fun updateExpense(expense: Expense)

    @Query("SELECT expense.uid, expense.debtId, expense.isForExpertMode, expense.comment, group_concat(contact.alias, \", \") as receiversList, expense.totalAmount FROM expense LEFT JOIN receiverwithamountfordb ON receiverwithamountfordb.expenseId = expense.uid INNER JOIN contact ON receiverwithamountfordb.contactId = contact.uid WHERE expense.uid IN (:expenseId) GROUP BY receiverwithamountfordb.expenseId")
    fun getExpenseByIds(expenseId: Int): Maybe<Expense>

    @Query("select SUM(totalAmount) from expense where debtId = :debtId AND isForExpertMode = 1")
    fun getExpensesSumForDebtAndExpertMode(debtId: Int): LiveData<Double>
}