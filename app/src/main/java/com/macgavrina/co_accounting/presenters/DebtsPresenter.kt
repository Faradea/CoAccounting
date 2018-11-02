package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.DebtsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.rxjava.Events

class DebtsPresenter: BasePresenter<DebtsContract.View>(), DebtsContract.Presenter, DebtsProvider.DatabaseCallback {

    override fun onContactsListLoaded(contactsList: List<com.macgavrina.co_accounting.room.Contact>) {
        getView()?.hideProgress()
        getView()?.initializeList(contactsList)
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

    override fun deleteDebtButtonIsPressed(selectedDebtsIds: List<Int>) {
        //ToDo Сделать массовое удаление контактов через actionMode
    }

    override fun debtItemIsSelected(selectedDebtId: Int) {
        Log.d("selectedDebtId = ${selectedDebtId}")
    }

}