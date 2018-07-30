package com.macgavrina.co_accounting.interfaces

public interface ProfileContract {

    interface View:BaseViewContract {

        fun finishSelf()

        fun showProgress()

        fun hideProgress()

        fun updateUserData(login:String?)
    }

    interface Presenter:BasePresenterContract<View> {

        fun logoutButtonIsPressed()

    }
}