package com.macgavrina.co_accounting.interfaces

interface ProfileContract {

    interface View:BaseViewContract {

        fun showProgress()

        fun hideProgress()

        fun updateUserData(login:String?)
    }

    interface Presenter:BasePresenterContract<View> {

        fun logoutButtonIsPressed()

    }
}