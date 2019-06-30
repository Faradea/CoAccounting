package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.macgavrina.co_accounting.support.STATUS_ACTIVE

@Entity
class CurrencyToTripRelation(currencyId: Int, tripId: Int) {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "tripId")
    var tripId: Int = tripId

    @ColumnInfo(name = "currencyId")
    var currencyId: Int = currencyId

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null

    @ColumnInfo(name = "status")
    var status: Int = STATUS_ACTIVE
}