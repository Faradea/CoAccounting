package com.macgavrina.co_accounting.presenters

import android.util.Log
import com.macgavrina.co_accounting.interfaces.LoginContract
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import android.provider.ContactsContract.CommonDataKinds.Note
import com.macgavrina.co_accounting.model.AuthResponse
import io.reactivex.observers.DisposableSingleObserver


//ToDo При повороте экрана приложение не падает, но результат логина "теряется" (а хорошо бы продолжить отображать прогресс-бар и продолжить процесс)
class LoginPresenter: BasePresenter<LoginContract.View>(), LoginContract.Presenter {
    override fun inputTextFieldsAreEmpty(areFilled: Boolean) {
        loginButtonEnabled = areFilled
        getView()?.setLoginButtonEnabled(loginButtonEnabled)
    }

    //ToDo BUG Что-то не так с активностью кнопки Login при повороте экрана
    var loginButtonEnabled: Boolean = false

    override fun viewIsReady() {
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

                    authService.performPostCallWithQuery(login!!, pass!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableSingleObserver<AuthResponse>() {
                                override fun onSuccess(t: AuthResponse) {
                                    loginButtonEnabled = true
                                    getView()?.setLoginButtonEnabled(loginButtonEnabled)
                                    getView()?.hideProgress()
                                    Log.d("InDebtApp", "Pass is ok, token = ${t.userToken}")
                                    UserProvider().saveUserData(User(login, t.userToken))
                                    getView()?.finishSelf()
                                }

                                override fun onError(e: Throwable) {
                                    loginButtonEnabled = true
                                    getView()?.setLoginButtonEnabled(loginButtonEnabled)
                                    getView()?.hideProgress()
                                    getView()?.displayToast(e.message!!)
                                    Log.d("InDebtApp", "Pass is NOK, error = ${e.message}")

                                }
                            })
            }

        }

    }

