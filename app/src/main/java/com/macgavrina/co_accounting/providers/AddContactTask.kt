package com.macgavrina.co_accounting.providers

import android.os.AsyncTask
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact

/*
class AddContactTask(callback: AddContactTaskCallback) : AsyncTask<Contact, Void, Void>() {

    override fun doInBackground(contact: Contact): Void {
        MainApplication.db.contactDAO().insertContact(contact)
        Log.d("contact is saved in DB")
    }


    //ToDo использовать RxJava вместо AsyncTask

    val myCallback:AddContactTaskCallback = callback

    interface AddContactTaskCallback {

    }

}*/
