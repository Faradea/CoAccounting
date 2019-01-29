package com.macgavrina.co_accounting.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.AddContactContract
import com.macgavrina.co_accounting.presenters.ContactPresenter
import kotlinx.android.synthetic.main.contact_fragment.*
import kotlinx.android.synthetic.main.debt_activity.*
import android.text.Editable

class ContactActivity : AppCompatActivity(), AddContactContract.View {

    lateinit var presenter: ContactPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = ContactPresenter()
        presenter.attachView(this)
        presenter.viewIsCreated()

        contact_fragment_delete_fab.setOnClickListener { view ->
            presenter.deleteButtonIsPressed()
        }

        val extras = intent.extras
        val contactId = extras?.getInt("contactId")

        if (contactId == -1) {
            presenter.contactIdIsReceiverFromMainActivity(null)
        } else {
            presenter.contactIdIsReceiverFromMainActivity(contactId.toString())
        }

        contact_fragment_email_et.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isEmpty()) {
                    contact_fragment_email_til.error = "Enter email"
                } else {
                    contact_fragment_email_til.error = null
                }
            }
        })

        contact_fragment_name_et.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isEmpty()) {
                    contact_fragment_name_til.error = "Enter name"
                } else {
                    contact_fragment_name_til.error = null
                }
            }
        })


    }

    override fun onResume() {
        super.onResume()

        presenter.viewIsReady()
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    //For back button in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {

                if (!getEmail().isNullOrEmpty() && !getAlias().isNullOrEmpty()) {
                    presenter.doneButtonIsPressed()
                    return true
                } else {

                    if (getEmail().isNullOrEmpty()) {
                        contact_fragment_email_til.error = "Enter email"
                    }

                    if (getAlias().isNullOrEmpty()) {
                        contact_fragment_name_til.error = "Enter name"
                    }

                    return true
                }
            }
        }

        onBackPressed()
        return true
    }

    //For action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return true
    }

    override fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun displayToast(text: String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun finishSelf() {
        onBackPressed()
    }

    override fun getAlias(): String {
        return contact_fragment_name_et.text.toString()
    }

    override fun getEmail(): String {
        return contact_fragment_email_et.text.toString()
    }

    override fun showProgress() {
        contact_fragment_progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        contact_fragment_progressBar.visibility = View.INVISIBLE
    }

    override fun displayContactData(alias: String, email: String) {
        contact_fragment_name_et.setText(alias)
        contact_fragment_email_et.setText(email)
    }

    override fun displayAlert(text: String, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(text)
                .setTitle(title)
                .setPositiveButton("ok") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    override fun hideDeleteButton() {
        contact_fragment_delete_fab.hide()
    }
}