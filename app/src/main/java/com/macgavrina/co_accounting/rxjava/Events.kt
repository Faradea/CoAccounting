package com.macgavrina.co_accounting.rxjava

import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.room.Debt
import com.macgavrina.co_accounting.room.Trip

object Events {
    class TripIsAdded(inputTrip: Trip) {
        val trip = inputTrip
    }

    class TripIsUpdated(inputTripId: String, inputIsCurrent: Boolean) {
        val tripId = inputTripId
        val isCurrent = inputIsCurrent
    }

    class OnClickTripList(inputTripId: String) {
        val tripId = inputTripId
    }

    //ToDo REFACT add subcategories
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

    class ContactEditingIsFinished{}

    class ContactIsDeleted(inputContact: Contact) {
        val contact = inputContact
    }

    class DebtIsDeleted(inputDebt: Debt) {
        val debt = inputDebt
    }

    class OnClickDebtItemList(uid: String?) {
        val myUid = uid
    }

    class AddDebt

    class DebtIsAdded

    class AddTrip

    class AddDebtReceiverWithAmountListIsChanged(positionInList: Int, newText: String) {
        val myPositionInList = positionInList
        val myNewText = newText
    }

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

    class OnClickExpenseItemList(expenseId: Int, debtId: Int) {
        val myExpenseId = expenseId
        val myDebtId = debtId
    }

    class onClickSelectedReceiverOnAddExpenseFragment(contact: Contact) {
        val myContact = contact
    }

    class DeletedContactIsRestored

    class DeletedDebtIsRestored

    class OnClickSwitchTripList(inputTripId: String, inputSwitchIsChecked: Boolean) {
        val tripId = inputTripId
        val switchIsChecked = inputSwitchIsChecked
    }

    class TripIsDeleted(inputTrip: Trip) {
        val trip = inputTrip
    }

    class OnClickCheckboxContactList(inputContactId: String, inputIsChecked: Boolean) {
        val contactId = inputContactId
        val isChecked = inputIsChecked
    }

    class ContactCannotBeDisableForTrip {

    }

}