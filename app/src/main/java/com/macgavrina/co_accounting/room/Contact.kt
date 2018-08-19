package com.macgavrina.co_accounting.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Contact {
    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
    set
    get

    @ColumnInfo(name = "email")
    public var email: String? = null
    set
    get

    @ColumnInfo(name = "alias")
    public var alias: String? = null
    set
    get

    @ColumnInfo(name = "friendId")
    public var friendId: String? = null
    set
    get
}