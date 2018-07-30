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



class LoginPresenter: BasePresenter<LoginContract.View>(), LoginContract.Presenter {


    override fun viewIsReady() {
        getView()!!.setLoginButtonEnabled(false)
        getView()!!.hideProgress()
    }

    override fun loginButtonIsPressed() {

        getView()!!.setLoginButtonEnabled(false)
        getView()!!.hideKeyboard()
        getView()!!.showProgress()
        val login:String = getView()!!.getLoginFromEditText()
        val pass:String = getView()!!.getPasswordFromEditText()

        val checkIfInputIsNotEmpty: Boolean = (login.length != 0) and (pass.length != 0)
        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()

            authService.performPostCallWithQuery(login, pass)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<AuthResponse>() {
                        override fun onSuccess(t: AuthResponse) {
                            getView()!!.setLoginButtonEnabled(true)
                            getView()!!.hideProgress()
                            Log.d("InDebtApp", "Pass is ok, token = ${t.userToken}")
                            UserProvider().saveUserData(User(login, t.userToken))
                            getView()!!.finishSelf()
                        }

                        override fun onError(e: Throwable) {
                            getView()!!.setLoginButtonEnabled(true)
                            getView()!!.hideProgress()
                            Log.d("InDebtApp", "Pass is NOK, error = ${e.message}")

                            getView()!!.displayToast(e.message!!)

                        }
                    })

        }

    }
}

