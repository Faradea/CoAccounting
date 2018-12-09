package com.macgavrina.co_accounting.room

import androidx.room.*
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

    @Query("DELETE FROM receiverwithamountfordb WHERE expenseId IN (:expenseId)")
    fun deleteReceiversWithAmountForExpense(expenseId: String)

    @Query ("SELECT COUNT(*) FROM receiverwithamountfordb WHERE contactId IN (:contactId)")
    fun checkReceiverWithAmountForContact(contactId: String): Single<Int>

    @Insert
    fun insertReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg receiverForAmountForDB: ReceiverWithAmountForDB)

    @Delete
    fun deleteReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Update
    fun updateReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)
}