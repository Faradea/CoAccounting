package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ContactsContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.Contact
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.rxjava.RxBus

class ContactsPresenter: BasePresenter<ContactsContract.View>(), ContactsContract.Presenter {
    override fun viewIsReady() {

        Log.d("view is ready")
        val contact1:Contact = Contact("123", "321", "email@mail.ru", "My Best Friend", "555")
        val contact2:Contact = Contact("123", "321", "email@mail.ru", "My Best Friend 2", "555")
        val contact3:Contact = Contact("123", "321", "email@mail.ru", "My Best Friend 3", "555")
        val contactsList: List<Contact> = mutableListOf(contact1, contact2, contact3)

        getView()?.initializeList(contactsList)

    }

    override fun addContactButtonIsPressed() {
        Log.d("is pressed")
/*        MainApplication
                .bus
                .send(Events.EventTypeOne())*/
    }

    override fun deleteContactsButtonIsPressed(selectedContactsIds: List<Int>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun contactItemIsSelected(selectedContactId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}