package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.EditContactContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.schedulers.Schedulers

class EditContactPresenter: BasePresenter<EditContactContract.View>(), EditContactContract.Presenter {

    override fun viewIsReady() {
    }

    var saveButtonEnabled: Boolean = false
    lateinit var contact:Contact

    override fun aliasIsChanged() {

        if (getView()?.getAliasFromEditText().equals(contact.alias)) {
            saveButtonEnabled = false
        } else {
            saveButtonEnabled = true
        }
        getView()?.setSaveButtonEnabled(saveButtonEnabled)
    }

    override fun viewIsReady(uid: String) {

        getView()?.setSaveButtonEnabled(saveButtonEnabled)
        getView()?.showProgress()

        if (uid.length != 0) {
            //ToDo перенести в ContactProvider
            MainApplication.db.contactDAO().loadContactByIds(uid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableMaybeObserver<Contact>() {
                        override fun onSuccess(t: com.macgavrina.co_accounting.room.Contact) {
                            contact = t
                            getView()?.displayContactData(t.alias!!, t.email!!)
                        }

                        override fun onError(e: Throwable) {
                            Log.d(e.toString())
                        }

                        override fun onComplete() {
                            Log.d("nothing")
                        }
                    })
        }

        getView()?.setSaveButtonEnabled(saveButtonEnabled)
        getView()?.hideProgress()

    }

    override fun saveButtonIsPressed() {

        saveButtonEnabled = false
        getView()?.setSaveButtonEnabled(saveButtonEnabled)
        getView()?.hideKeyboard()
        getView()?.showProgress()

        val alias: String? = getView()?.getAliasFromEditText()

        var checkIfInputIsNotEmpty: Boolean = false
        if (alias != null) {
            checkIfInputIsNotEmpty = alias.isNotEmpty()
        }

        if (checkIfInputIsNotEmpty) {

            contact.alias = getView()?.getAliasFromEditText()

            //ToDo дописать сохранение данных в базу
            Log.d("save data")

        }

    }
}