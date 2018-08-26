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

        fun hideProgress()

        fun displayContactsFragment()

        fun displayAddContactFragment()

        fun displayEditContactFragment(uid: String?)

    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

        fun gotoContactsEvent()

    }

}