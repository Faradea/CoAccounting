package com.macgavrina.co_accounting.presenters

import android.util.Log
import com.macgavrina.co_accounting.interfaces.LoginContract
import com.macgavrina.co_accounting.model.AuthResult
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPresenter: BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    override fun viewIsReady() {
    }

    override fun loginButtonIsPressed() {

        getView()!!.hideKeyboard()
        val login:String = getView()!!.getLoginFromEditText()
        val pass:String = getView()!!.getPasswordFromEditText()

        val checkIfInputIsNotEmpty: Boolean = (login.length != 0) and (pass.length != 0)
        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()
            val call = authService.performPostCallWithQuery(login, pass)

            call.enqueue(object : Callback<AuthResult> {
                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    val authResult: AuthResult? = response.body()
                    if (authResult != null) {
                        Log.d("InDebtApp", authResult.userToken)
                        UserProvider().saveUserData(User(login, authResult.userToken))
                    } else {
                        Log.d("InDebtApp", "userToken = null")
                        getView()!!.displayToast("Wrong login or password")
                    }
                }

                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    Log.d("InDebtApp", "server error")
                    getView()!!.displayToast("Server error")
                }
            })
        }

    }
}

