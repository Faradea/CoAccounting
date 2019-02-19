package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Currency {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0

    @ColumnInfo(name = "name")
    public var name: String? = null

    @ColumnInfo(name = "symbol")
    public var symbol: String? = null

    public var activeTripId: Int = -1
}