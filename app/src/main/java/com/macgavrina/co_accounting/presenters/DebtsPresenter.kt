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

        Log.d("view is ready")

        getView()?.showProgress()

        getAndDisplayAllDebts()

        getView()?.hideProgress()

    }

    override fun addDebtButtonIsPressed() {
        Log.d("is pressed")
        MainApplication.bus.send(Events.AddDebt())
    }

    override fun debtItemIsSelected(selectedDebtId: Int) {
        Log.d("selectedDebtId = ${selectedDebtId}")
    }

    private fun getAndDisplayAllDebts() {
        MainApplication.db.debtDAO().getAll("active")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableMaybeObserver<List<Debt>>() {
                    override fun onSuccess(debtsList: List<Debt>) {
                        getView()?.hideProgress()
                        Log.d("debtsList = $debtsList")
                        getView()?.initializeList(debtsList)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("error, ${e.toString()}")
                    }

                    override fun onComplete() {
                        //ToDo REFACT call dispose() here and in all onComplete
                        Log.d("nothing")
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

                                getView()?.showProgress()

                                getAndDisplayAllDebts()

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

        lastDeletedDebt!!.status = "active"
        Completable.fromAction {
            MainApplication.db.debtDAO().updateDebt(lastDeletedDebt!!)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onComplete() {
                        lastDeletedDebt = null
                        MainApplication.bus.send(Events.DeletedDebtIsRestored())
                    }

                    override fun onError(e: Throwable) {
                        Log.d("Error restoring debt, $e")
                    }
                })
    }

}