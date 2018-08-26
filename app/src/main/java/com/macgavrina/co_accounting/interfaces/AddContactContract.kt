package com.macgavrina.co_accounting.interfaces

interface AddContactContract {

    interface View:BaseViewContract {

        fun getAlias():String

        fun getEmail():String

        fun showProgress()

        fun hideProgress()

        fun setAddButtonEnabled(areEnabled: Boolean)

        fun hideKeyboard()

        fun displayToast(text: String)
    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun inputTextFieldsAreEmpty(areFilled: Boolean)

    }
}