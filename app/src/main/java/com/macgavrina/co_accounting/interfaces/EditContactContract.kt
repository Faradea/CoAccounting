package com.macgavrina.co_accounting.interfaces

interface EditContactContract {

    interface View:BaseViewContract {

        fun hideKeyboard()

        fun getAliasFromEditText(): String

        fun setSaveButtonEnabled(isSaveButtonEnabled: Boolean)

        fun displayToast(text:String)

        fun showProgress()

        fun hideProgress()

        fun displayContactData(alias: String, email: String)

    }

    interface Presenter:BasePresenterContract<View> {

        fun saveButtonIsPressed()

        fun aliasIsChanged()

        fun viewIsReady(uid: String)

    }
}