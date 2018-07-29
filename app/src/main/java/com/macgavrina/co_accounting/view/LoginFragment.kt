package com.macgavrina.co_accounting.view

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.model.AuthResult
import com.macgavrina.co_accounting.services.AuthService
import kotlinx.android.synthetic.main.login_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.inputmethod.InputMethodManager
import com.macgavrina.co_accounting.interfaces.LoginContract
import com.macgavrina.co_accounting.rxjava.LoginInputObserver.LoginInputObserver.getTextWatcherObservable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import com.macgavrina.co_accounting.presenters.LoginPresenter

class LoginFragment:Fragment(), LoginContract.View {

    lateinit var loginPresenter: LoginPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        loginPresenter = LoginPresenter()
        loginPresenter.attachView(this)

        return inflater.inflate(R.layout.login_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loginPresenter.viewIsReady()

        login_fragment_login_button.setOnClickListener { view ->
            loginPresenter.loginButtonIsPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        val emailObservable: Observable<String> = getTextWatcherObservable(login_fragment_login_et)
        val passwordObservable:Observable<String> = getTextWatcherObservable(login_fragment_password_et)

        val isSignInEnabled: Observable<Boolean> = Observable.combineLatest(
                emailObservable,
                passwordObservable,
                BiFunction { u, p -> u.isNotEmpty() && p.isNotEmpty() })

        isSignInEnabled.subscribe {it ->
            setLoginButtonEnabled(it)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginPresenter.detachView()
    }

    override fun hideKeyboard() {
        val inputMethodManager:InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun getLoginFromEditText(): String {
        return login_fragment_login_et.text.toString()
    }

    override fun getPasswordFromEditText(): String {
        return login_fragment_password_et.text.toString()
    }

    override fun setLoginButtonEnabled(isLoginButtonEnabled: Boolean) {
        login_fragment_login_button.isEnabled = isLoginButtonEnabled
    }

    override fun showProgress() {
        //ToDo дописать отображение прогресс-бара
    }

    override fun hideProgress() {
        //ToDo дописать скрывание прогресс-бара
    }

    override fun displayToast(text:String) {
        Toast.makeText(context, "Wrong login or password", Toast.LENGTH_SHORT).show()
    }

}