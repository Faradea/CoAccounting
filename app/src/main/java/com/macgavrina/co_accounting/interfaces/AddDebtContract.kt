package com.macgavrina.co_accounting.interfaces

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

        fun setupReceiverSpinner(contactsList: Array<String?>)
    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun inputTextFieldsAreEmpty(areFilled: Boolean)

    }
}