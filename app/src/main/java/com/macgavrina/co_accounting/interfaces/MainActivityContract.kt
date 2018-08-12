package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.presenters.LoginPresenter

interface MainActivityContract {

    interface View:BaseViewContract {

        fun displayLoginFragment(enteredLogin: String?)

        fun displayMainFragment()

        fun hideMenu()

        fun showProgress()

        fun displayProfileFragment()

        fun displayRecoverPassFragment(enteredLogin: String?)

        fun displayRegisterFragment(enteredLogin: String?)

        fun updateLoginText(login:String)

        fun displayRegisterSuccessDialog(title: String, text: String)

        fun displayRecoverPassSuccessDialog(title: String, text: String, enteredLogin: String?)

    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

        fun logoutFinished()

        fun loginFinished(nextFragment: LoginPresenter.nextFragment, enteredLogin: String?)

        fun passRecoverIsSuccessfull(title: String, text: String, enteredLogin: String?)

        fun registrationIsSuccessfull(title: String, text: String)

        fun gotoLoginEvent(enteredLogin: String?)

    }

}