package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Contact

interface ContactsContract {

    interface View:BaseViewContract {

        fun displayRevertChangesAction()

        fun initializeList(contactsList: List<Contact>, tripId: Int)

        fun updateList()

        fun showProgress()

        fun hideProgress()

        fun displayToast(text:String)
    }

    interface Presenter:BasePresenterContract<View> {

        fun addContactButtonIsPressed()

        fun contactItemIsSelected(selectedContactId:Int)

    }
}