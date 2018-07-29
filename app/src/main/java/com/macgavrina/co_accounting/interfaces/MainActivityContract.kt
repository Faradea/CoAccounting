package com.macgavrina.co_accounting.interfaces

interface MainActivityContract {

    interface View:BaseViewContract {

        fun displayLoginFragment()

        fun hideMenu()

        fun showProgress()

        fun hideProgress()

    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

    }

}