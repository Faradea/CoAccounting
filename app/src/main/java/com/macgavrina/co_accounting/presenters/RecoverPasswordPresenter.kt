package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.RecoverPasswordContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.RecoverPassResponse
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.services.AuthService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class RecoverPasswordPresenter: BasePresenter<RecoverPasswordContract.View>(), RecoverPasswordContract.Presenter {

    private var subscriptionToBus: Disposable? = null

    var isNextButtonEnabled: Boolean = false

    override fun inputTextFieldsAreEmpty(isEmpty: Boolean) {
        isNextButtonEnabled = isEmpty
        getView()?.setNextButtonEnabled(isNextButtonEnabled)
    }

    override fun viewIsReady() {
        isNextButtonEnabled = getView()?.getEmailFromEditText()?.length!! > 0
        getView()?.hideProgress()
        getView()?.setNextButtonEnabled(isNextButtonEnabled)
    }

    override fun detachView() {
        super.detachView()

        unsubscribeFromEventBus()
    }

    override fun nextButtonIsPressed() {
        getView()?.hideKeyboard()
        getView()?.showProgress()
        isNextButtonEnabled = false
        getView()?.setNextButtonEnabled(isNextButtonEnabled)
        val email: String? = getView()?.getEmailFromEditText()

        var checkIfInputIsNotEmpty: Boolean = false
        if (email != null) {
            checkIfInputIsNotEmpty = email.isNotEmpty()
        }

        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()

            if (subscriptionToBus == null) {
                subscriptionToBus = authService.recoverPassCall(email!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<RecoverPassResponse>() {
                            override fun onSuccess(t: RecoverPassResponse) {
                                getView()?.hideProgress()
                                Log.d("Recover pass is ok")
                                MainApplication.bus.send(Events.RecoverPassIsSuccessful("Pass recovering is ok", "Link to recover password is sent to your email", getView()?.getEmailFromEditText()))
                                unsubscribeFromEventBus()
                            }

                            override fun onError(e: Throwable) {
                                getView()?.hideProgress()
                                getView()?.displayToast(e.message!!)
                                Log.d("Recover pass is NOK, error = ${e.message}")
                                unsubscribeFromEventBus()
                            }
                        })
            }
        }

    }

    private fun unsubscribeFromEventBus() {
        if (subscriptionToBus != null) {
            subscriptionToBus?.dispose()
            subscriptionToBus = null
        }
    }
}
