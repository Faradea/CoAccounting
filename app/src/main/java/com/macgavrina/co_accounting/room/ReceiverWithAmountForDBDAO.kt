package com.macgavrina.co_accounting.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ReceiverWithAmountForDBDAO {

    @get:Query("SELECT * FROM receiverwithamountfordb")
    val getAll: Maybe<List<ReceiverWithAmountForDB>>

//    @Query("SELECT * FROM receivers WHERE uid IN (:contactId)")
//    fun loadContactByIds(contactId: String): Maybe<Contact>

    @Query("SELECT * FROM receiverwithamountfordb WHERE expenseId IN (:expenseId) ORDER BY uid")
    fun getReceiversWithAmountForExpense(expenseId: String): Maybe<List<ReceiverWithAmountForDB>>

    @Query("SELECT * FROM receiverwithamountfordb WHERE expenseId IN (:expenseId) ORDER BY uid")
    fun getReceiversWithAmountForExpenseLiveData(expenseId: Int): LiveData<List<ReceiverWithAmountForDB>>

    @Query("DELETE FROM receiverwithamountfordb WHERE expenseId IN (:expenseId)")
    fun deleteReceiversWithAmountForExpense(expenseId: String): Completable

    @Query ("SELECT COUNT(*) FROM receiverwithamountfordb WHERE contactId IN (:contactId)")
    fun checkReceiverWithAmountForContact(contactId: String): Single<Int>

    @Query ("SELECT COUNT(*) FROM receiverwithamountfordb INNER JOIN expense ON Expense.uid = ReceiverWithAmountForDB.expenseId INNER JOIN Debt ON Expense.debtId = Debt.uid INNER JOIN trip On debt.tripId = trip.uid WHERE contactId IN (:contactId) AND debt.status IN (\"active\") AND trip.isCurrent = 1 AND trip.status = \"active\"")
    fun checkReceiverWithAmountForContactAndCurrentTrip(contactId: String): Single<Int>

    @Insert
    fun insertReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg receiverForAmountForDB: ReceiverWithAmountForDB)

    @Delete
    fun deleteReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Update
    fun updateReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)
}