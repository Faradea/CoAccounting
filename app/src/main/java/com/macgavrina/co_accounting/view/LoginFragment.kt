package com.macgavrina.co_accounting.view

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.model.AuthResult
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.services.AuthService
import kotlinx.android.synthetic.main.login_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginFragment:Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        login_fragment_login_button.setOnClickListener { view ->

            val checkIfInputIsNotEmpty: Boolean = (login_fragment_login_et.text.length != 0) and (login_fragment_password_et.text.length != 0)
            if (checkIfInputIsNotEmpty) {
                val login: String = login_fragment_login_et.text.toString()
                val pass: String = login_fragment_password_et.text.toString()

                val authService: AuthService = AuthService.create()
                val call = authService.performPostCallWithQuery(login, pass)

                call.enqueue(object : Callback<AuthResult> {
                    override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                        val authResult:AuthResult? = response.body()
                        if (authResult != null) {
                            Log.d("InDebtApp", authResult.userToken)
                        } else
                            Log.d("InDebtApp", "userToken = null")
                            Toast.makeText(context, "Wrong login or password", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                        Log.d("InDebtApp", "server error")
                        Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                    }
                })
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.login_fragment, container,
                false)
    }

}