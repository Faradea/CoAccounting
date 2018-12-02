package com.macgavrina.co_accounting.customsync

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.DefaultServiceResponse
import com.macgavrina.co_accounting.model.RecoverPassResponse
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.services.AuthService
import com.macgavrina.co_accounting.services.ContactsService
import com.macgavrina.co_accounting.sync.SyncAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class SyncService() {

    //ToDo REFACT сделать singleton и syncronized syncData

    companion object {

        const val SYNC_UPLOAD = "upload"
        const val SYNC_DOWNLOAD = "download"

        fun syncData(syncUpload: Boolean, syncDownload: Boolean, userToken: String) {

            if (syncUpload) {
                Log.d("start syncUpload")
                val contactsService: ContactsService = ContactsService.create()

                contactsService.addContact(userToken, "testEmail", "testAlias")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<DefaultServiceResponse>() {
                            override fun onSuccess(t: DefaultServiceResponse) {
                                Log.d("test contact is added")
                            }

                            override fun onError(e: Throwable) {
                                Log.d("adding test contact failed with error = ${e.message}")

                            }
                        })

            }

            if (syncDownload) {
                Log.d("syncDownload")
            }

        }
    }
}