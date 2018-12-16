package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.interfaces.DebtsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.providers.ContactsProvider
import com.macgavrina.co_accounting.providers.DebtsProvider
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.disposables.Disposable

class DebtsPresenter: BasePresenter<DebtsContract.View>(), DebtsContract.Presenter, DebtsProvider.DatabaseCallback {

    private var lastDeletedDebt: Debt? = null
    private var subscriptionToBus: Disposable? = null

    override fun onDebtsListLoaded(debtsList: List<com.macgavrina.co_accounting.room.Debt>) {
        getView()?.hideProgress()
        Log.d("debtsList = $debtsList")
        getView()?.initializeList(debtsList)
    }

    override fun attachView(baseViewContract: DebtsContract.View) {
        super.attachView(baseViewContract)
        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
    }

    override fun onDatabaseError() {
        getView()?.displayToast("Database error")
    }

    override fun viewIsReady() {

        Log.d("view is ready")

        getView()?.showProgress()

        DebtsProvider().getAll(this)

        getView()?.hideProgress()

    }

    override fun addDebtButtonIsPressed() {
        Log.d("is pressed")
        MainApplication.bus.send(Events.AddDebt())
    }

    override fun debtItemIsSelected(selectedDebtId: Int) {
        Log.d("selectedDebtId = ${selectedDebtId}")
    }


    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.DeletedDebtIsRestored -> {

                                getView()?.showProgress()

                                DebtsProvider().getAll(this)

                                getView()?.hideProgress()
                            }

                            is Events.DebtIsDeleted -> {
                                lastDeletedDebt = `object`.debt
                                getView()?.displayOnDeleteDebtSnackBar()
                            }
                        }
                    }
        }
    }

    private fun unsubscribeFromEventBus() {
        if (subscriptionToBus != null) {
            subscriptionToBus?.dispose()
            subscriptionToBus = null
        }
    }

    override fun undoDeleteDebtButtonIsPressed() {
        if (lastDeletedDebt == null) return

        DebtsProvider().restoreDebt(this, lastDeletedDebt!!)
    }

    override fun onDebtRestored() {
        super.onDebtRestored()
        lastDeletedDebt = null
        MainApplication.bus.send(Events.DeletedDebtIsRestored())
    }

}