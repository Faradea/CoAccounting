package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import com.macgavrina.co_accounting.support.STATUS_DELETED
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ReceiverWithAmountForDBDAO {

    @get:Query("SELECT * FROM receiverwithamountfordb WHERE status = $STATUS_ACTIVE")
    val getAll: Maybe<List<ReceiverWithAmountForDB>>

//    @Query("SELECT * FROM receivers WHERE uid IN (:contactId)")
//    fun loadContactByIds(contactId: String): Maybe<Contact>

    @Query("SELECT * FROM receiverwithamountfordb WHERE expenseId IN (:expenseId) AND status = $STATUS_ACTIVE ORDER BY uid")
    fun getReceiversWithAmountForExpense(expenseId: String): Maybe<List<ReceiverWithAmountForDB>>

    @Query("SELECT * FROM receiverwithamountfordb WHERE expenseId IN (:expenseId) AND status = $STATUS_ACTIVE ORDER BY uid")
    fun getReceiversWithAmountForExpenseLiveData(expenseId: Int): LiveData<List<ReceiverWithAmountForDB>>

    @Query("UPDATE receiverwithamountfordb SET status = $STATUS_DELETED WHERE expenseId IN (:expenseId)")
    fun deleteReceiversWithAmountForExpense(expenseId: String): Single<Int>

    @Query("UPDATE receiverwithamountfordb SET status = $STATUS_DELETED WHERE expenseId IN (:expenseId)")
    fun deleteReceiversWithAmountForExpenseWithoutResult(expenseId: String): Completable

    @Query ("SELECT COUNT(*) FROM receiverwithamountfordb WHERE contactId IN (:contactId) AND status = $STATUS_ACTIVE")
    fun checkReceiverWithAmountForContact(contactId: String): Single<Int>

    @Query ("SELECT COUNT(*) FROM receiverwithamountfordb " +
            "INNER JOIN expense ON Expense.uid = ReceiverWithAmountForDB.expenseId " +
            "INNER JOIN Debt ON Expense.debtId = Debt.uid " +
            "INNER JOIN trip On debt.tripId = trip.uid " +
            "WHERE contactId IN (:contactId) " +
            "AND debt.status = $STATUS_ACTIVE AND trip.isCurrent = 1 AND trip.status = $STATUS_ACTIVE " +
            "AND expense.status = $STATUS_ACTIVE " +
            "AND receiverwithamountfordb.status = $STATUS_ACTIVE")
    fun checkReceiverWithAmountForContactAndCurrentTrip(contactId: String): Single<Int>

    @Insert
    fun insertReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg receiverForAmountForDB: ReceiverWithAmountForDB)

    @Query("UPDATE receiverwithamountfordb SET status = $STATUS_DELETED WHERE uid = :receiverForAmountForDBId")
    fun deleteReceiverWithAmount(receiverForAmountForDBId: Int)

    @Update
    fun updateReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)
}