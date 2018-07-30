package com.macgavrina.co_accounting.interfaces

interface MainActivityContract {

    interface View:BaseViewContract {

        fun displayLoginFragment()

        fun displayMainFragment()

        fun hideMenu()

        fun showProgress()

        fun displayProfileFragment()

        fun updateLoginText(login:String)

    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

        fun logoutFinished()

        fun loginFinished()

    }

}