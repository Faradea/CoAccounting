package com.macgavrina.co_accounting.interfaces

interface MainActivityContract {

    interface View:BaseViewContract {

        fun displayLoginFragment(enteredLogin: String?)

        fun displayMainFragment()

        fun hideMenu()

        fun showProgress()

        fun displayProfileFragment()

        fun displayRecoverPassFragment(enteredLogin: String?)

        fun displayRegisterFragment(enteredLogin: String?)

        fun updateLoginText(login:String)

        fun displayRegisterSuccessDialog(title: String, text: String)

        fun displayRecoverPassSuccessDialog(title: String, text: String, enteredLogin: String?)

        fun hideProgress()

        fun displayContactsFragment()

        fun displayAddContactFragment(contactId: String?)

        fun displayEditContactFragment(uid: String?)

        fun displayDebtsFragment()

        fun displayAddDebtFragment(myUid: String?)

        fun displayAddReceiverInAddDebtFragment(myUid: Int, myExpenseId: Int?)

        fun dismissAddReceiverInAddDebtFragment()

        fun displayAddReceiverInAddDebtFragmentAfterReceiverAdded()

        fun displayToast(text: String)

        fun displayOnDeleteContactSnackBar()

        fun displayOnDeleteDebtSnackBar()

    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

        fun gotoContactsEvent()

        fun gotoDebts()

        fun addReceiverInAddDebtFragmentAfterReceiverAddedIsDisplayed()

        fun undoDeleteContactButtonIsPressed()

        fun undoDeleteDebtButtonIsPressed()

    }

}