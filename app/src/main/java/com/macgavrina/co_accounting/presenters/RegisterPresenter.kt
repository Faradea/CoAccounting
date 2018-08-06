package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.RecoverPasswordContract
import com.macgavrina.co_accounting.interfaces.RegisterContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.AuthResponse
import com.macgavrina.co_accounting.model.RecoverPassResponse
import com.macgavrina.co_accounting.model.RegisterResponse
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class RegisterPresenter: BasePresenter<RegisterContract.View>(), RegisterContract.Presenter {
    override fun gotoLoginButtonIsPressed() {
        getView()?.finishSelf()
    }

    var isRegisterButtonEnabled:Boolean = false

    override fun inputTextFieldsAreEmpty(isEmpty: Boolean) {
        isRegisterButtonEnabled = isEmpty
        getView()?.setRegisterButtonEnabled(isRegisterButtonEnabled)
    }

    override fun viewIsReady() {
        if (getView()?.getEmailFromEditText()?.length!! > 0) {
            isRegisterButtonEnabled = true
        } else {
            isRegisterButtonEnabled = false
        }
        getView()?.hideProgress()
        getView()?.setRegisterButtonEnabled(isRegisterButtonEnabled)
    }


    override fun registerButtonIsPressed() {

        Log.d("Register button is pressed")

        getView()?.hideKeyboard()
        getView()?.showProgress()
        isRegisterButtonEnabled = false
        getView()?.setRegisterButtonEnabled(isRegisterButtonEnabled)

        val login: String? = getView()?.getEmailFromEditText()
        val pass: String? = getView()?.getPassFromEditText()

        var checkIfInputIsNotEmpty: Boolean = false
        if (login != null) {
            if (pass != null) {
                checkIfInputIsNotEmpty = (login.length != 0) and (pass.length != 0)
            }
        }

        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()

            authService.registerPostCall(login!!, pass!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<RegisterResponse>() {
                        override fun onSuccess(t: RegisterResponse) {
                            Log.d("Register is ok")

                            authService.authCall(login!!, pass!!)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeWith(object : DisposableSingleObserver<AuthResponse>() {
                                        override fun onSuccess(t: AuthResponse) {
                                            getView()?.hideProgress()
                                            Log.d("Auth after register is ok, token = ${t.userToken}")
                                            UserProvider().saveUserData(User(login, t.userToken))
                                            getView()?.displayDialog("Registration is ok", "Account is created in inactive mode. Please activate your account by link sent to your email.")
                                        }

                                        override fun onError(e: Throwable) {
                                            getView()?.hideProgress()
                                            getView()?.displayToast(e.message!!)
                                            Log.d("Auth after registration is NOK, error = ${e.message}")

                                        }
                                    })

                        }

                        override fun onError(e: Throwable) {
                            getView()?.hideProgress()
                            getView()?.displayToast(e.message!!)
                            Log.d("Registration is NOK, error = ${e.message}")

                        }
                    })
        }
    }
}