package com.macgavrina.co_accounting.interfaces

public interface RecoverPasswordContract {

    interface View:BaseViewContract {

        fun hideKeyboard()

        fun getEmailFromEditText(): String

        fun displayDialog(text:String)

        fun showProgress()

        fun hideProgress()

    }

    interface Presenter:BasePresenterContract<View> {

        fun nextButtonIsPressed()

    }
}