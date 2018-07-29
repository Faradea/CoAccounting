package com.macgavrina.co_accounting.interfaces

public interface LoginContract {

    interface View:BaseViewContract {

        fun hideKeyboard()

        fun getLoginFromEditText(): String

        fun getPasswordFromEditText(): String

        fun setLoginButtonEnabled(isLoginButtonEnabled: Boolean)

        fun displayToast(text:String)

        fun showProgress()

        fun hideProgress()
    }

    interface Presenter:BasePresenterContract<View> {

        fun loginButtonIsPressed()

    }
}