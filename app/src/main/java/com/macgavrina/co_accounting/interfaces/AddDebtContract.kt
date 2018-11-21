package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.model.ReceiverWithAmount
import com.macgavrina.co_accounting.room.Expense

interface AddDebtContract {

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

    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun addReceiverButtonIsPressed()

        fun inputTextFieldsAreEmpty(areFilled: Boolean)

        fun debtIdIsReceiverFromMainActivity(debtId: Int?)

    }
}