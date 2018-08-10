package com.macgavrina.co_accounting.view

import android.content.Context
import android.support.v4.app.Fragment
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
import io.reactivex.functions.BiFunction
import com.macgavrina.co_accounting.presenters.LoginPresenter
import kotlinx.android.synthetic.main.register_fragment.*

class LoginFragment:Fragment(), LoginContract.View {

    lateinit var loginPresenter: LoginPresenter

    interface OnLoginFinishedListener {
        fun loginFinished(nextFragment: LoginPresenter.nextFragment, enteredLogin: String?)
    }

    lateinit var onLoginFinishedListener: OnLoginFinishedListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            onLoginFinishedListener = activity as OnLoginFinishedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnLoginFinishedListener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        loginPresenter = LoginPresenter()
        loginPresenter.attachView(this)

        return inflater.inflate(R.layout.login_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        login_fragment_login_button.setOnClickListener { view ->
            loginPresenter.loginButtonIsPressed()
        }

        login_fragment_recover_password_tv.setOnClickListener {view ->
            Log.d("Recover password button is pressed")
            loginPresenter.recoverPassButtonIsPressed()
        }

        login_fragment_register_tv.setOnClickListener { view ->
            loginPresenter.registerButtonIsPressed()
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
            loginPresenter.inputTextFieldsAreEmpty(it)
        }


        val enteredLogin:String? = this.arguments?.getString("enteredLogin")
        if (enteredLogin != null) {
            login_fragment_login_et.setText("${enteredLogin}")
        }

        loginPresenter.viewIsReady()

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
        login_fragment_progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        login_fragment_progressBar.visibility = View.INVISIBLE
    }

    override fun displayToast(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun finishSelf(nextFragment: LoginPresenter.nextFragment, enteredLogin: String?) {
        onLoginFinishedListener.loginFinished(nextFragment, enteredLogin)
    }

}