package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Expense

interface DebtActivityContract {

    interface View:BaseViewContract {
        fun getSender(): Int

        fun getAmount(): String

        fun getDate(): String

        fun getTime(): String

        fun getComment(): String

        fun setSender(contactId: Int)

        fun setAmount(amount: String)

        fun setDate(date: String)

        fun setTime(time: String)

        fun setComment(comment: String)

        fun showProgress()

        fun hideProgress()

        fun hideKeyboard()

        fun displayToast(text: String)

        fun setupSenderSpinner(contactsList: Array<String?>)

        fun initializeExpensesList(expenseList: List<Expense>)

        fun finishSelf()

        fun displayExpenseActivity(debtId: Int, expenseId: Int?)

        fun showAlertAndGoToContacts(alertText: String)


    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun addReceiverButtonIsPressed()

        fun inputTextFieldsAreEmpty(areFilled: Boolean)

        fun debtIdIsReceiverFromMainActivity(debtId: Int?)

        fun deleteButtonIsPressed()

        fun viewIsPaused()

        fun saveDebtDraft()

    }

}