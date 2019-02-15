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

        fun displayDebtsFragment(isInitial: Boolean)

        fun displayAddDebtFragment(myUid: String?)

        fun displayAddReceiverInAddDebtFragment(myUid: Int, myExpenseId: Int?)

        fun dismissAddReceiverInAddDebtFragment()

        fun displayAddReceiverInAddDebtFragmentAfterReceiverAdded()

        fun displayToast(text: String)

        fun displayOnDeleteContactSnackBar()

        fun startActivityToShareAllData(dataToShare: String)

        fun displayTripsFragment()

        fun displayAddTripFragment(tripId: String?)

        fun displayAlert(text: String, title: String)

        fun displayCalculationsFragment()
    }

    interface Presenter:BasePresenterContract<View> {

        fun headerIsClicked()

        fun gotoContactsEvent()

        fun gotoDebts(isInitial: Boolean)

        fun gotoTrips()

        fun addReceiverInAddDebtFragmentAfterReceiverAddedIsDisplayed()

        fun undoDeleteContactButtonIsPressed()

        fun prepareAndShareData()

        fun gotoCalculations()
    }

}