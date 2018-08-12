package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.LoginContract
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.AuthResponse
import io.reactivex.observers.DisposableSingleObserver

//ToDo При повороте экрана приложение не падает, но результат логина "теряется" (а хорошо бы продолжить отображать прогресс-бар и продолжить процесс)
class LoginPresenter: BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    enum class nextFragment(i: Int) {
        MAIN(0),
        RECOVER_PASS(1),
        REGISTER(2)
    }

    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        loginButtonEnabled = areFilled
        getView()?.setLoginButtonEnabled(loginButtonEnabled)
    }

    //ToDo BUG Что-то не так с активностью кнопки Login и остальные

    var loginButtonEnabled: Boolean = false

    override fun viewIsReady() {
        if (getView()?.getLoginFromEditText()?.length!! > 0) {
            if (getView()?.getPasswordFromEditText()?.length!! > 0) {
                loginButtonEnabled = true
            }
            //loginButtonEnabled = false
        } else {
            loginButtonEnabled = false
        }

        Log.d("loginButtonIsEnabled = ${loginButtonEnabled}")
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
                checkIfInputIsNotEmpty = (login.length != 0) and (pass.length != 0)
            }
        }

        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()

                    authService.authCall(login!!, pass!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableSingleObserver<AuthResponse>() {
                                override fun onSuccess(t: AuthResponse) {
                                    loginButtonEnabled = true
                                    getView()?.setLoginButtonEnabled(loginButtonEnabled)
                                    getView()?.hideProgress()
                                    Log.d("Pass is ok, token = ${t.userToken}")
                                    UserProvider().saveUserData(User(login, t.userToken))
                                    getView()?.finishSelf(nextFragment.MAIN, null)
                                }

                                override fun onError(e: Throwable) {
                                    loginButtonEnabled = true
                                    getView()?.setLoginButtonEnabled(loginButtonEnabled)
                                    getView()?.hideProgress()
                                    getView()?.displayToast(e.message!!)
                                    Log.d("Pass is NOK, error = ${e.message}")

                                }
                            })
            }

        }

    override fun recoverPassButtonIsPressed() {
        val enteredLogin = getView()?.getLoginFromEditText()
        getView()?.finishSelf(nextFragment.RECOVER_PASS, enteredLogin)
        Log.d("nextFragment.RECOVER_PASS = ${nextFragment.RECOVER_PASS}")
    }

    override fun registerButtonIsPressed() {
        val enteredLogin:String? = getView()?.getLoginFromEditText()
        getView()?.finishSelf(nextFragment.REGISTER, enteredLogin)
    }
}
