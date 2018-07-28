package com.macgavrina.co_accounting.presenters

import android.util.Log
import android.widget.Toast
import com.macgavrina.co_accounting.interfaces.LoginContractView
import com.macgavrina.co_accounting.model.AuthResult
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.services.AuthService
import com.macgavrina.co_accounting.view.LoginFragment
import kotlinx.android.synthetic.main.login_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPresenter() {

    var view:LoginContractView? = null

    fun attachView(loginFragment: LoginFragment) {
        view = loginFragment
    }

    fun detachView() {
        view = null
    }

    fun viewIsReady() {
        view!!.hideKeyboard()
    }

    fun loginButtonIsPressed() {

        view!!.hideKeyboard()
        val login:String = view!!.getLoginFromEditText()
        val pass:String = view!!.getPasswordFromEditText()

        val checkIfInputIsNotEmpty: Boolean = (login.length != 0) and (pass.length != 0)
        if (checkIfInputIsNotEmpty) {

            val authService: AuthService = AuthService.create()
            val call = authService.performPostCallWithQuery(login, pass)

            call.enqueue(object : Callback<AuthResult> {
                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    val authResult: AuthResult? = response.body()
                    if (authResult != null) {
                        Log.d("InDebtApp", authResult.userToken)
                        UserProvider().saveUserData(User(login, pass))
                    } else {
                        Log.d("InDebtApp", "userToken = null")
                        view!!.displayToast("Wrong login or password")
                    }
                }

                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    Log.d("InDebtApp", "server error")
                    view!!.displayToast("Server error")
                }
            })
        }

    }
}

