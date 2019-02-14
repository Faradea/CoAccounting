package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        indices = [Index(value = arrayOf("tripId", "contactId"), unique = true)]
)

class ContactToTripRelation(contactId: Int, tripId: Int) {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "tripId")
    public var tripId: Int? = tripId
        set
        get

    @ColumnInfo(name = "contactId")
    public var contactId: Int? = contactId
        set
        get
}