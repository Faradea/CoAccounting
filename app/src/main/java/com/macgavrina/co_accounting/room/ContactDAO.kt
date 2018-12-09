package com.macgavrina.co_accounting.room

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface ContactDAO {
    @Query("SELECT * FROM contact WHERE status IN (:status)")
    fun getAll(status: String): Maybe<List<Contact>>

    @Query("SELECT * FROM contact WHERE uid IN (:contactId)")
    fun loadContactByIds(contactId: String): Maybe<Contact>

    @Insert
    fun insertContact(contact: Contact)

    @Query("UPDATE contact SET status = :status WHERE uid IN (:contactId)")
    fun deleteContact(contactId: String, status: String)

    @Update
    fun updateContact(contact:Contact)
}