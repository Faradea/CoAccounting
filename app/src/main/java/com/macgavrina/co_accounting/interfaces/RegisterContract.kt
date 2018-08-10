package com.macgavrina.co_accounting.interfaces

public interface RegisterContract {

    interface View:BaseViewContract {

        fun hideKeyboard()

        fun getEmailFromEditText(): String

        fun getPassFromEditText(): String

        fun displayDialog(title: String, text:String)

        fun showProgress()

        fun hideProgress()

        fun displayToast(text: String)

        fun setRegisterButtonEnabled(isNextButonEnabled: Boolean)

        fun finishSelf(enteredLogin: String?)

    }

    interface Presenter:BasePresenterContract<View> {

        fun registerButtonIsPressed()

        fun inputTextFieldsAreEmpty(isEmpty: Boolean)

        fun gotoLoginButtonIsPressed()

    }
}