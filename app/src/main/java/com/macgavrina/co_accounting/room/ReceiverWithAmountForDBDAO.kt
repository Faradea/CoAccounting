package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe

@Dao
interface ReceiverWithAmountForDBDAO {

    @get:Query("SELECT * FROM receiverwithamountfordb")
    val getAll: Maybe<List<ReceiverWithAmountForDB>>

//    @Query("SELECT * FROM receivers WHERE uid IN (:contactId)")
//    fun loadContactByIds(contactId: String): Maybe<Contact>

    @Query("SELECT * FROM receiverwithamountfordb WHERE expenseId IN (:expenseId) ORDER BY uid")
    fun getReceiversWithAmountForExpense(expenseId: String): Maybe<List<ReceiverWithAmountForDB>>

    @Insert
    fun insertReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg receiverForAmountForDB: ReceiverWithAmountForDB)

    @Delete
    fun deleteReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Update
    fun updateReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)
}