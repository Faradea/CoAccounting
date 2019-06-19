package com.macgavrina.co_accounting.rxjava

import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip

object Events {

    class OnClickTripList(inputTripId: String) {
        val tripId = inputTripId
    }

    class FromRegisterToLoginEvent(enteredLogin: String?) {
        val myEnteredLogin = enteredLogin
    }

    class RegisterIsSuccessful(title: String, text: String) {
        val myTitle = title
        val myText = text
    }

    class RecoverPassIsSuccessful(title: String, text: String, enteredLogin: String?) {
        val myTitle = title
        val myText = text
        val myEnteredLogin = enteredLogin
    }

    class LogoutFinished{}

    class LoginIsSuccessful{}

    class FromLoginToRecoverPass(enteredLogin: String?){
        val myEnteredLogin = enteredLogin
    }

    class FromLoginToRegister(enteredLogin: String?){
        val myEnteredLogin = enteredLogin
    }

    class AddContact{}

    class ContactIsAdded{}

    class OnClickContactList(uid: String?) {
        val myUid: String? = uid
    }

    class ContactIsDeleted(inputContact: Contact) {
        val contact = inputContact
    }

    class OnClickDebtItemList(uid: String?) {
        val myUid = uid
    }

    class AddDebt

    class DebtIsAdded

    class AddTrip

    class AddReceiverButtonInAddDebtFragment(uid: Int) {
        val myUid = uid
    }

    class NewContactIsAddedToSelectedReceiversList(contact: Contact?) {
        val myContact = contact
    }

    class HideAddReceiverInAddDebtFragment(withSaveChanges: Boolean) {
        val myWithSaveChanges = withSaveChanges
    }

    class ReceiversWithAmountInAddDebtIsSaved

    class AddDebtFragmentRequiresRefresh

    class OnClickSelectedReceiverOnAddExpenseFragment(contact: Contact) {
        val myContact = contact
    }

    class DeletedContactIsRestored

    class OnClickSwitchTripList(inputTripId: String, inputSwitchIsChecked: Boolean) {
        val tripId = inputTripId
        val switchIsChecked = inputSwitchIsChecked
    }

    class OnClickCheckboxCurrency(val currencyId: Int, val isChecked: Boolean)

}