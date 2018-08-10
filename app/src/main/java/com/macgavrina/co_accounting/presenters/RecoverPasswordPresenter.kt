package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.RecoverPasswordContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.AuthResponse
import com.macgavrina.co_accounting.model.RecoverPassResponse
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class RecoverPasswordPresenter: BasePresenter<RecoverPasswordContract.View>(), RecoverPasswordContract.Presenter {
    override fun inputTextFieldsAreEmpty(isEmpty: Boolean) {
        isNextButtonEnabled = isEmpty
        getView()?.setNextButtonEnabled(isNextButtonEnabled)
    }

    var isNextButtonEnabled:Boolean = false

    override fun viewIsReady() {
        if (getView()?.getEmailFromEditText()?.length!! > 0) {
            isNextButtonEnabled = true
        } else {
            isNextButtonEnabled = false
        }
        getView()?.hideProgress()
        getView()?.setNextButtonEnabled(isNextButtonEnabled)
    }



    override fun nextButtonIsPressed() {
        Log.d("Next button is pressed")

        getView()?.hideKeyboard()
        getView()?.showProgress()
        isNextButtonEnabled = false
        getView()?.setNextButtonEnabled(isNextButtonEnabled)
        val email: String? = getView()?.getEmailFromEditText()

        var checkIfInputIsNotEmpty: Boolean = false
        if (email != null) {
                checkIfInputIsNotEmpty = email.length != 0
        }

        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()

            authService.recoverPassCall(email!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<RecoverPassResponse>() {
                        override fun onSuccess(t: RecoverPassResponse) {
                            getView()?.hideProgress()
                            Log.d("Recover pass is ok")
                            getView()?.displayDialog("Pass recovering is ok", "Link to recover password is sent to your email")
                            //getView()?.finishSelf(LoginPresenter.nextFragment.MAIN)
                        }

                        override fun onError(e: Throwable) {
                            getView()?.hideProgress()
                            getView()?.displayToast(e.message!!)
                            Log.d("Recover pass is NOK, error = ${e.message}")

                        }
                    })
        }
    }
}