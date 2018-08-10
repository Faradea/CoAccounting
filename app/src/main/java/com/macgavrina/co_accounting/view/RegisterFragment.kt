package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.RegisterContract
import com.macgavrina.co_accounting.presenters.RegisterPresenter
import com.macgavrina.co_accounting.rxjava.LoginInputObserver
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.register_fragment.*

class RegisterFragment() : Fragment(), RegisterContract.View {
    override fun finishSelf(enteredLogin: String?) {
        onRegisterEventsListener.finishSelf(enteredLogin)
    }

    interface OnRegisterEventsListener {
        fun registrationIsSuccessful(title: String, text: String)
        fun finishSelf(enteredLogin: String?)
    }

    lateinit var presenter: RegisterPresenter
    lateinit var onRegisterEventsListener: OnRegisterEventsListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            onRegisterEventsListener = activity as OnRegisterEventsListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnRegisterEventsListener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = RegisterPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.register_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        register_fragment_next_button.setOnClickListener { view ->
            presenter.registerButtonIsPressed()
        }

        register_fragment_goto_login_tv.setOnClickListener { view ->
            presenter.gotoLoginButtonIsPressed()
        }

    }

    override fun onResume() {
        super.onResume()

        val emailObservable: Observable<String> = LoginInputObserver.getTextWatcherObservable(register_fragment_email_edit_text)
        val passwordObservable: Observable<String> = LoginInputObserver.getTextWatcherObservable(register_fragment_pass_edit_text)

        val isRegisterEnabled: Observable<Boolean> = Observable.combineLatest(
                emailObservable,
                passwordObservable,
                BiFunction { u, p -> u.isNotEmpty() && p.isNotEmpty() })

        isRegisterEnabled.subscribe {it ->
            presenter.inputTextFieldsAreEmpty(it)
        }

        val enteredLogin:String? = this.arguments?.getString("enteredLogin")
        if (enteredLogin != null) {
            register_fragment_email_edit_text.setText("${enteredLogin}")
        }

        presenter.viewIsReady()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun showProgress() {
        register_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        register_fragment_progress_bar.visibility = View.INVISIBLE
    }

    override fun getEmailFromEditText(): String {
        return register_fragment_email_edit_text.text.toString()
    }

    override fun getPassFromEditText(): String {
        return register_fragment_pass_edit_text.text.toString()
    }

    override fun displayDialog(title:String, text: String) {
        onRegisterEventsListener.registrationIsSuccessful(title, text)
    }

    override fun setRegisterButtonEnabled(isNextButonEnabled: Boolean) {
        register_fragment_next_button.isEnabled = isNextButonEnabled
    }

    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }
}