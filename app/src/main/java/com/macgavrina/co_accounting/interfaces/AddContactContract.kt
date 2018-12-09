package com.macgavrina.co_accounting.interfaces

interface AddContactContract {

    interface View:BaseViewContract {

        fun getAlias():String

        fun getEmail():String

        fun showProgress()

        fun hideProgress()

        fun hideKeyboard()

        fun displayToast(text: String)

        fun finishSelf()

        fun displayContactData(alias: String, email: String)

        fun displayAlert(text: String, title: String)

        fun hideDeleteButton()
    }

    interface Presenter:BasePresenterContract<View> {

        fun addButtonIsPressed()

        fun deleteButtonIsPressed()

        fun contactIdIsReceiverFromMainActivity(contactId: String?)

    }
}