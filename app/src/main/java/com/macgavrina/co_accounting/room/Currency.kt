package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.macgavrina.co_accounting.support.STATUS_ACTIVE

@Entity
class Currency {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "name")
    var name: String = ""

    @ColumnInfo(name = "symbol")
    var symbol: String = ""

    var activeTripId: Int = -1

    var lastUsedCurrencyId: Int = -1

    var isActiveForCurrentTrip: Boolean = false

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null

    @ColumnInfo(name = "status")
    var status: Int = STATUS_ACTIVE

    override fun toString(): String {
        return "uid = $uid, name = $name, symbol = $symbol, activeTripId = $activeTripId, lastUsedCurrencyId = $lastUsedCurrencyId, isActiveForCurrentTrip = $isActiveForCurrentTrip"
    }
}