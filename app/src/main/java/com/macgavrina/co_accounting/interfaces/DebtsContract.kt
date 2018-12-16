package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Debt

interface DebtsContract {

    interface View:BaseViewContract {

        fun displayRevertChangesAction()

        fun initializeList(debtsList: List<Debt>)

        fun updateList()

        fun showProgress()

        fun hideProgress()

        fun displayToast(text:String)

        fun displayOnDeleteDebtSnackBar()
    }

    interface Presenter:BasePresenterContract<View> {

        fun addDebtButtonIsPressed()

        fun debtItemIsSelected(selectedContactId:Int)

        fun undoDeleteDebtButtonIsPressed()

    }
}