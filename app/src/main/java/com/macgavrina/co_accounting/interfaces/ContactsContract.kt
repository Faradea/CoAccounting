package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.model.Contact
import com.macgavrina.co_accounting.presenters.LoginPresenter

interface ContactsContract {

    interface View:BaseViewContract {

        fun displayRevertChangesAction()

        fun initializeList(contactsList: List<Contact>)

        fun updateList()

        fun showProgress()

        fun hideProgress()

        fun finishSelf()
    }

    interface Presenter:BasePresenterContract<View> {

        fun addContactButtonIsPressed()

        fun deleteContactsButtonIsPressed(selectedContactsIds:List<Int>)

        fun contactItemIsSelected(selectedContactId:Int)

    }
}