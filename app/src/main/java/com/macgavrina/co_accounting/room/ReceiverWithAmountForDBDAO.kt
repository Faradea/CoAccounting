package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Maybe

@Dao
interface ReceiverWithAmountForDBDAO {

    @get:Query("SELECT * FROM receiverwithamountfordb")
    val getAll: Maybe<List<ReceiverWithAmountForDB>>

//    @Query("SELECT * FROM receivers WHERE uid IN (:contactId)")
//    fun loadContactByIds(contactId: String): Maybe<Contact>

    @Insert
    fun insertReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg receiverForAmountForDB: ReceiverWithAmountForDB)

    @Delete
    fun deleteReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)

    @Update
    fun updateReceiverWithAmount(receiverForAmountForDB: ReceiverWithAmountForDB)
}