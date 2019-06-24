package com.macgavrina.co_accounting.repositories

import androidx.lifecycle.LiveData
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.ContactToTripRelation
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.DebtDAO
import com.macgavrina.co_accounting.support.MoneyFormatter
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class DebtRepository {

    private var debtDao: DebtDAO = MainApplication.db.debtDAO()

    private var allDebtsForCurrentTrip: LiveData<List<Debt>>

    init {
        allDebtsForCurrentTrip = debtDao.getDebtsForCurrentTrip()
    }

    fun getAllDebtsForCurrentTrip(): LiveData<List<Debt>> {
        return allDebtsForCurrentTrip
    }

    fun getDebtById(debtId: Int): LiveData<Debt> {
        return debtDao.getDebtByIds(debtId)
    }

    fun getDebtByIdRx(debtId: Int): Maybe<Debt> {
        return debtDao.getDebtByIdRx(debtId)
    }

    fun getDebtDraft(): LiveData<Debt> {
        return debtDao.getDebtDraft()
    }

    fun getDebtDraftRx(): Maybe<Debt> {
        return debtDao.getDebtDraftRx()
    }

    fun updateDebtInDB(debt: Debt) {

        debt.spentAmount = MoneyFormatter.justRound(debt.spentAmount)

        TripRepository().getCurrentTrip()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({ maybeTrip ->
                    maybeTrip
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe ({ trip ->
                                debt.tripId = trip.uid
                                Completable.fromAction {
                                    debtDao.updateDebt(debt)
                                }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({
                                            Log.d("Debt is updated, debt = $debt")
                                        }, { error ->
                                            Log.d("Error updating debt, $error")
                                        })
                            }, {error ->
                                Log.d("Error getting current trip from DB, $error")
                            })
                }, {error ->
                    Log.d("Error getting current trip from DB, $error")
                })
    }

    fun createDebtDraft(): Completable {
        val debt = Debt()
        debt.status = "draft"
        return Completable.fromAction {
            debtDao.insertDebt(debt)
        }
    }

    fun deleteDebt(debt: Debt) {
        Completable.fromAction {
            debtDao.deleteDebt(debt.uid)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.d("Debt is deleted, debt = $debt")
                }, { error ->
                    Log.d("Error deleting debt, $error")
                })
    }

    fun checkDebtCorrectness(debt: Debt) {

        Log.d("Checking debt correctness")
        ExpenseRepository().getExpensesTotalAmountForDebt(debt.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe ({ expensesTotalAmount ->
                    Log.d("expensesTotalAmount = $expensesTotalAmount")
                    if (expensesTotalAmount == debt.spentAmount) {
                        if (!debt.isCorrect) {
                            debt.isCorrect = true
                            updateDebtInDB(debt)
                        }
                    } else {
                        if (debt.isCorrect) {
                            debt.isCorrect = false
                            updateDebtInDB(debt)
                        }
                    }
                }, {error ->
                    Log.d("Error getting expenses total from db, $error")
                    if (debt.isCorrect) {
                        debt.isCorrect = false
                        updateDebtInDB(debt)
                    }
                })
    }

}