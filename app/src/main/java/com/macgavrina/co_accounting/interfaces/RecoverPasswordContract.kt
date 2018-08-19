package com.macgavrina.co_accounting.interfaces

interface RecoverPasswordContract {

    interface View:BaseViewContract {

        fun hideKeyboard()

        fun getEmailFromEditText(): String

        fun showProgress()

        fun hideProgress()

        fun displayToast(text:String)

        fun setNextButtonEnabled(isNextButonEnabled: Boolean)

    }

    interface Presenter:BasePresenterContract<View> {

        fun nextButtonIsPressed()

        fun inputTextFieldsAreEmpty(isEmpty: Boolean)

    }
}