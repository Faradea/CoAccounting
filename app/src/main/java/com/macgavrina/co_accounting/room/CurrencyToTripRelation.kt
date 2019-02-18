package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        indices = [Index(value = arrayOf("currencyId", "tripId"), unique = true)]
)

class CurrencyToTripRelation(currencyId: Int, tripId: Int) {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "tripId")
    public var tripId: Int? = tripId
        set
        get

    @ColumnInfo(name = "currencyId")
    public var currencyId: Int? = currencyId
        set
        get
}