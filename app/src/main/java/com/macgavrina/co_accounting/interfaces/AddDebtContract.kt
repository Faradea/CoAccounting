package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.model.ReceiverWithAmount

interface AddDebtContract {

    interface View:BaseViewContract {

        fun getSender():String

        fun getReceiver(): String

        fun getAmount(): String

        fun getDate(): String

        fun getComment(): String

        fun showProgress()

        fun hideProgress()

        fun setAddButtonEnabled(areEnabled: Boolean)

        fun hideKeyboard()

        fun displayToast(text: String)

        fun setupSenderSpinner(contactsList: Array<String?>)

        fun initializeReceiversList(receiverWithAmountList: List<ReceiverWithAmount>)

    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun addReceiverButtonIsPressed()

        fun inputTextFieldsAreEmpty(areFilled: Boolean)

    }
}