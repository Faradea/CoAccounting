package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.room.Contact

interface AddReceiverInAddDebtContract {

    interface View:BaseViewContract {

//        fun showProgress()
//
//        fun hideProgress()

        fun hideKeyboard()

        fun initializeNotSelectedReceiversList(contactsList: List<Contact>?)

        fun initializeSelectedReceiversList(contactsList: List<Contact>?)

    }

    interface Presenter:BasePresenterContract<View> {

    }
}