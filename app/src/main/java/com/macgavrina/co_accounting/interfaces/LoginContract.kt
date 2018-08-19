package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.presenters.LoginPresenter

interface LoginContract {

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

        fun inputTextFieldsAreEmpty(areFilled:Boolean)

        fun recoverPassButtonIsPressed()

        fun registerButtonIsPressed()

    }
}