package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.interfaces.DebtsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class DebtsPresenter: BasePresenter<DebtsContract.View>(), DebtsContract.Presenter {

    private var lastDeletedDebt: Debt? = null
    private var subscriptionToBus: Disposable? = null

    override fun attachView(baseViewContract: DebtsContract.View) {
        super.attachView(baseViewContract)
        subscribeToEventBus()
    }

    override fun detachView() {
        super.detachView()
        unsubscribeFromEventBus()
    }

    override fun viewIsReady() {
        getAndDisplayAllDebts()
    }

    override fun addDebtButtonIsPressed() {
        Log.d("Add debt button is pressed")
        MainApplication.bus.send(Events.AddDebt())
    }

    override fun debtItemIsSelected(selectedDebtId: Int) {
        Log.d("selectedDebtId = ${selectedDebtId}")
    }

    private fun getAndDisplayAllDebts() {

        Log.d("Getting debts from DB...")

        getView()?.showProgress()

        MainApplication.db.debtDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onSuccess(debtsList: List<Debt>) {
                        Log.d("Debts are received from DB, size = ${debtsList.size}")
                        getView()?.hideProgress()
                        getView()?.initializeList(debtsList)
                    }

                    override fun onError(e: Throwable) {
                        getView()?.hideProgress()
                        Log.d("Error getting debts list from DB, $e")
                    }

                    override fun onComplete() {
                        getView()?.hideProgress()
                    }
                })
    }

    private fun subscribeToEventBus() {
        if (subscriptionToBus == null) {
            subscriptionToBus = MainApplication
                    .bus
                    .toObservable()
                    .subscribe { `object` ->
                        when (`object`) {
                            is Events.DeletedDebtIsRestored -> {
                                Log.d("Catch Events.DeletedDebtIsRestore event, updating debts list...")
                                getAndDisplayAllDebts()
                            }

                            is Events.DebtIsDeleted -> {
                                Log.d("Catch Events.DebtIsDeleted event, display snackbar for cancel...")
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

        Log.d("Undo delete button is pressed")
        if (lastDeletedDebt == null) return

        lastDeletedDebt!!.status = "active"
        Completable.fromAction {
            MainApplication.db.debtDAO().updateDebt(lastDeletedDebt!!)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        Log.d("Debt is restored")
                        lastDeletedDebt = null
                        MainApplication.bus.send(Events.DeletedDebtIsRestored())
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error restoring debt, $e")
                    }
                })
    }

}