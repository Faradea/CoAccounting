package com.macgavrina.co_accounting.room

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface ContactDAO {
    @get:Query("SELECT * FROM contact")
    val getAll: Maybe<List<Contact>>

    @Query("SELECT * FROM contact WHERE uid IN (:contactId)")
    fun loadContactByIds(contactId: String): Maybe<Contact>

    @Insert
    fun insertContact(contact: Contact)

    @Delete
    fun deleteContact(contact: Contact)

    @Delete
    fun deleteContacts(vararg contacts: Contact)

    @Update
    fun updateContact(contact:Contact)
}