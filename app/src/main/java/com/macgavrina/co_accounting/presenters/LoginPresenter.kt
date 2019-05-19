package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.DebtActivityContract
import com.macgavrina.co_accounting.interfaces.LoginContract
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.AuthResponse
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver

class LoginPresenter: BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    private var subscriptionToBus: Disposable? = null

    var loginButtonEnabled: Boolean = false

    enum class nextFragment(i: Int) {
        MAIN(0),
        RECOVER_PASS(1),
        REGISTER(2)
    }

    override fun detachView() {
        super.detachView()
        unsubscribeFromEventBus()
    }

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        loginButtonEnabled = areFilled
        getView()?.setLoginButtonEnabled(areFilled)
    }

    override fun viewIsReady() {

        if (getView()?.getLoginFromEditText()?.length!! > 0) {
            loginButtonEnabled = getView()?.getPasswordFromEditText()?.length!! > 0
        } else {
            loginButtonEnabled = false
        }

        getView()?.setLoginButtonEnabled(loginButtonEnabled)
        getView()?.hideProgress()

    }

    override fun loginButtonIsPressed() {

        loginButtonEnabled = false
        getView()?.setLoginButtonEnabled(loginButtonEnabled)
        getView()?.hideKeyboard()
        getView()?.showProgress()

        val login: String? = getView()?.getLoginFromEditText()
        val pass: String? = getView()?.getPasswordFromEditText()

        var checkIfInputIsNotEmpty: Boolean = false
        if (login != null) {
            if (pass != null) {
                checkIfInputIsNotEmpty = login.isNotEmpty() and pass.isNotEmpty()
            }
        }

        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()

            if (subscriptionToBus == null) {
                subscriptionToBus = authService.authCall(login!!, pass!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<AuthResponse>() {
                            override fun onSuccess(t: AuthResponse) {
                                loginButtonEnabled = true
                                getView()?.setLoginButtonEnabled(loginButtonEnabled)
                                getView()?.hideProgress()
                                Log.d("Pass is ok, token = ${t.userToken}")
                                UserProvider().saveUserData(User(login, t.userToken))
                                MainApplication.bus.send(Events.LoginIsSuccessful())
                                unsubscribeFromEventBus()
                            }

                            override fun onError(e: Throwable) {
                                loginButtonEnabled = true
                                getView()?.setLoginButtonEnabled(loginButtonEnabled)
                                getView()?.hideProgress()
                                getView()?.displayToast(e.message!!)
                                Log.d("Pass is NOK, error = ${e.message}")
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

    override fun recoverPassButtonIsPressed() {
        val enteredLogin = getView()?.getLoginFromEditText()
        MainApplication.bus.send(Events.FromLoginToRecoverPass(enteredLogin))
    }

    override fun registerButtonIsPressed() {
        val enteredLogin:String? = getView()?.getLoginFromEditText()
        MainApplication.bus.send(Events.FromLoginToRegister(enteredLogin))
    }
}

