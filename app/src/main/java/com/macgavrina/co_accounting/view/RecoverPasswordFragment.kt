package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.RecoverPasswordContract
import com.macgavrina.co_accounting.presenters.RecoverPasswordPresenter
import com.macgavrina.co_accounting.rxjava.LoginInputObserver.LoginInputObserver.getTextWatcherObservable
import io.reactivex.Observable
import kotlinx.android.synthetic.main.recover_password_fragment.*


class RecoverPasswordFragment: Fragment(), RecoverPasswordContract.View {

    interface OnRecoverPasswordEventsListener {
        fun recoverIsSuccessfull(title: String, text: String, enteredLogin: String?)
    }

    lateinit var presenter: RecoverPasswordPresenter
    lateinit var onRecoverPasswordEventsListener: OnRecoverPasswordEventsListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            onRecoverPasswordEventsListener = activity as OnRecoverPasswordEventsListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnRecoverPasswordEventsListener")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = RecoverPasswordPresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.recover_password_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recover_password_fragment_next_button.setOnClickListener { view ->
            presenter.nextButtonIsPressed()
        }

        val enteredLogin:String? = this.arguments?.getString("enteredLogin")
        if (enteredLogin != null) {
            recover_password_fragment_edit_text.setText("${enteredLogin}")
        }
    }

    override fun onResume() {
        super.onResume()

        val emailObservable: Observable<String> = getTextWatcherObservable(recover_password_fragment_edit_text)
        emailObservable.subscribe { it ->
            presenter.inputTextFieldsAreEmpty(it.isNotEmpty())
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

    override fun getEmailFromEditText(): String {
        return recover_password_fragment_edit_text.text.toString()
    }

    override fun showProgress() {
        recover_password_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        recover_password_fragment_progress_bar.visibility = View.INVISIBLE
    }

    override fun displayDialog(title: String, text: String) {
        val enteredLogin: String? = recover_password_fragment_edit_text.text.toString()
        onRecoverPasswordEventsListener.recoverIsSuccessfull(title, text, enteredLogin)
    }

    override fun displayToast(text:String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun setNextButtonEnabled(isNextButtonEnabled: Boolean) {
        recover_password_fragment_next_button.isEnabled = isNextButtonEnabled
    }

}