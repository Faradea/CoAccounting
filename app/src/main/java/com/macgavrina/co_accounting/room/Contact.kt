package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters



@Entity
class Contact {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "email")
    var email: String = ""

    @ColumnInfo(name = "alias")
    var alias: String = ""

    @ColumnInfo(name = "status")
    var status: String = "active"

    var isActiveForCurrentTrip: Boolean = false
}