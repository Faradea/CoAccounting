package com.macgavrina.co_accounting.interfaces

interface RegisterContract {

    interface View:BaseViewContract {

        fun hideKeyboard()

        fun getEmailFromEditText(): String

        fun getPassFromEditText(): String

        fun showProgress()

        fun hideProgress()

        fun displayToast(text: String)

        fun setRegisterButtonEnabled(isNextButonEnabled: Boolean)

    }

    interface Presenter:BasePresenterContract<View> {

        fun registerButtonIsPressed()

        fun inputTextFieldsAreEmpty(isEmpty: Boolean)

        fun gotoLoginButtonIsPressed()

    }
}