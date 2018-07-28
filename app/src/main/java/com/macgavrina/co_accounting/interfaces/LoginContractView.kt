package com.macgavrina.co_accounting.interfaces

import com.macgavrina.co_accounting.model.User
import android.service.autofill.UserData


interface LoginContractView {

    fun hideKeyboard()

    fun getLoginFromEditText(): String

    fun getPasswordFromEditText(): String

    fun setLoginButtonEnabled(isLoginButtonEnabled: Boolean)

    fun displayToast(text:String)

    fun showProgress()

    fun hideProgress()
}