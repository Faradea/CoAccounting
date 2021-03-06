package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Contact

interface AddReceiverInAddDebtContract {

    interface View:BaseViewContract {

//        fun showProgress()
//
//        fun hideProgress()

        fun hideKeyboard()

        fun initializeNotSelectedReceiversList(contactsList: List<Contact>?)

        fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: Double)

        fun getAmount(): Double

        fun hideDeleteButton()

        fun showDeleteButton()

        fun setAmount(totalAmount: Double)

        fun finishSelf()

        fun getComment(): String
        fun setComment(comment: String)

        fun showAlert(alertText: String)
    }

    interface Presenter:BasePresenterContract<View> {

        fun amountIsEdited(newAmount: Double)

        fun cancelButtonInToolbarIsClicked()

        fun saveButtonIsPressed()

        fun debtIdIsReceiverFromMainActivity(debtId: Int)

        fun expenseIdIsReceivedFromMainActivity(expenseId: Int)

        fun deleteButtonIsPressed()

        fun userHasReadAlertAboutDeletingExpense()
    }
}