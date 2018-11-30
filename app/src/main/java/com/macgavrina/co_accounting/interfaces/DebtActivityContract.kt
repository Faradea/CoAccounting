package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Expense

interface DebtActivityContract {

    interface View:BaseViewContract {
        fun getSender():String

        fun getReceiver(): String

        fun getAmount(): String

        fun getDate(): String

        fun getComment(): String

        fun setSender(senderName: String)

        fun setAmount(amount: String)

        fun setDate(date: String)

        fun setComment(comment: String)

        fun showProgress()

        fun hideProgress()

        fun setAddButtonEnabled(areEnabled: Boolean)

        fun hideKeyboard()

        fun displayToast(text: String)

        fun setupSenderSpinner(contactsList: Array<String?>)

        fun initializeExpensesList(expenseList: List<Expense>)

        fun finishSelf()

        fun displayExpenseActivity(debtId: Int, expenseId: Int?)

    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun addReceiverButtonIsPressed()

        fun inputTextFieldsAreEmpty(areFilled: Boolean)

        fun debtIdIsReceiverFromMainActivity(debtId: Int?)

        fun deleteButtonIsPressed()

    }

}