package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.presenters.LoginPresenter
import com.macgavrina.co_accounting.room.Contact

interface ContactsContract {

    interface View:BaseViewContract {

        fun displayRevertChangesAction()

        fun initializeList(contactsList: List<Contact>)

        fun updateList()

        fun showProgress()

        fun hideProgress()

        fun displayToast(text:String)
    }

    interface Presenter:BasePresenterContract<View> {

        fun addContactButtonIsPressed()

        fun deleteContactsButtonIsPressed(selectedContactsIds:List<Int>)

        fun contactItemIsSelected(selectedContactId:Int)

    }
}