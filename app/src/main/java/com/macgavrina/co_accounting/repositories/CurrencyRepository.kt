package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.ContactDAO
import com.macgavrina.co_accounting.room.Currency
import com.macgavrina.co_accounting.room.CurrencyToTripRelationDAO

class CurrencyRepository {

    private var currencyToTripRelationDAO: CurrencyToTripRelationDAO = MainApplication.db.currencyToTripRelationDAO()

    init {
    }

    fun getAllCurrenciesForTrip(tripId: Int): LiveData<List<Currency>> {
        return currencyToTripRelationDAO.getAllCurrenciesWithUsedForTripMarker(tripId)
    }
}