package com.macgavrina.co_accounting.view

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.macgavrina.co_accounting.R
import kotlinx.android.synthetic.main.login_fragment.*
import android.view.inputmethod.InputMethodManager
import com.macgavrina.co_accounting.interfaces.LoginContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.rxjava.LoginInputObserver.LoginInputObserver.getTextWatcherObservable
import io.reactivex.Observable
import com.macgavrina.co_accounting.presenters.LoginPresenter

class LoginFragment: Fragment(), LoginContract.View {

    companion object {
        const val ENTERED_LOGIN_KEY = "enteredLogin"
    }

    lateinit var presenter: LoginPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = LoginPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.login_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val enteredLogin:String? = this.arguments?.getString(ENTERED_LOGIN_KEY)
        if (enteredLogin != null) {
            login_fragment_login_et.setText("${enteredLogin}")
        }

        login_fragment_login_button.setOnClickListener { view ->
            presenter.loginButtonIsPressed()
        }

        login_fragment_recover_password_tv.setOnClickListener {view ->
            presenter.recoverPassButtonIsPressed()
        }

        login_fragment_register_tv.setOnClickListener { view ->
            presenter.registerButtonIsPressed()
        }

    }

    override fun onResume() {
        super.onResume()

        val emailObservable: Observable<String> = getTextWatcherObservable(login_fragment_login_et)
        val passwordObservable:Observable<String> = getTextWatcherObservable(login_fragment_password_et)

        emailObservable.subscribe {it ->
            if (login_fragment_login_et.text.length != 0) {
                if (login_fragment_password_et.text.length != 0) {
                    presenter.inputTextFieldsAreEmpty(true)
                }
                else {
                    presenter.inputTextFieldsAreEmpty(false)
                }
            } else {
                presenter.inputTextFieldsAreEmpty(false)
            }
        }

        passwordObservable.subscribe {it ->
            if (login_fragment_login_et.text.length != 0) {
                if (login_fragment_password_et.text.length != 0) {
                    presenter.inputTextFieldsAreEmpty(true)
                }
                else {
                    presenter.inputTextFieldsAreEmpty(false)
                }
            } else {
                presenter.inputTextFieldsAreEmpty(false)
            }
        }

/*
        val isSignInEnabled: Observable<Boolean> = Observable.combineLatest(
                emailObservable,
                passwordObservable,
                BiFunction { u, p -> u.isNotEmpty() && p.isNotEmpty() })

        isSignInEnabled.subscribe {it ->
            loginPresenter.inputTextFieldsAreEmpty(it)
            Log.d("inputTextFieldsAreEmpty = ${it}")
        }

*/
        Log.d("viewIsReady")
        presenter.viewIsReady()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
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
        login_fragment_progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        login_fragment_progressBar.visibility = View.INVISIBLE
    }

    override fun displayToast(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}