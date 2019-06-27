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
    var uid: Int = 0

    @ColumnInfo(name = "tripId")
    var tripId: Int = tripId

    @ColumnInfo(name = "currencyId")
    var currencyId: Int = currencyId
}