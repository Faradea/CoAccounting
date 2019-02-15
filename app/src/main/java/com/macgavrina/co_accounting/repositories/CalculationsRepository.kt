package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.room.Calculation
import com.macgavrina.co_accounting.room.DebtDAO

class CalculationsRepository {

    private var debtDao: DebtDAO = MainApplication.db.debtDAO()

    private var allCalculationsForCurrentTrip: LiveData<List<Calculation>>

    init {
        allCalculationsForCurrentTrip = debtDao.getAllCalculationsForCurrentTrip()
    }

    fun getAllCalculationsForCurrentTrip(): LiveData<List<Calculation>> {
        return allCalculationsForCurrentTrip
    }

}