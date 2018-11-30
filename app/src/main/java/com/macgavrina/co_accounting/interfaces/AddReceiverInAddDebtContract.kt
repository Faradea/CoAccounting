package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Contact

interface AddReceiverInAddDebtContract {

    interface View:BaseViewContract {

//        fun showProgress()
//
//        fun hideProgress()

        fun hideKeyboard()

        fun initializeNotSelectedReceiversList(contactsList: List<Contact>?)

        fun initializeSelectedReceiversList(contactsList: List<Contact>?, amountPerPerson: Float)

        fun getAmount(): Float

        fun hideDeleteButton()

        fun showDeleteButton()

        fun setAmount(totalAmount: String?)

        fun finishSelf()

    }

    interface Presenter:BasePresenterContract<View> {

        fun amountIsEdited(newAmount: Float)

        fun cancelButtonInToolbarIsClicked()

        fun saveButtonIsPressed()

        fun debtIdIsReceiverFromMainActivity(debtId: Int)

        fun expenseIdIsReceivedFromMainActivity(expenseId: Int)

        fun deleteButtonIsPressed()

    }
}