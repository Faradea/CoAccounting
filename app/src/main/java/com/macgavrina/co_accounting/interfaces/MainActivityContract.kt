package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.presenters.LoginPresenter

interface MainActivityContract {

    interface View:BaseViewContract {

        fun displayLoginFragment()

        fun displayMainFragment()

        fun hideMenu()

        fun showProgress()

        fun displayProfileFragment()

        fun displayRecoverPassFragment()

        fun displayRegisterFragment()

        fun updateLoginText(login:String)

        fun displayRegisterSuccessDialog(title: String, text: String)

        fun displayRecoverPassSuccessDialog(title: String, text: String)

    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

        fun logoutFinished()

        fun loginFinished(nextFragment: LoginPresenter.nextFragment)

        fun passRecoverIsSuccessfull(title: String, text: String)

        fun registrationIsSuccessfull(title: String, text: String)

        fun gotoLoginEvent()

    }

}