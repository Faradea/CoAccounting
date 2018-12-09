package com.macgavrina.co_accounting.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.AddContactContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.presenters.AddContactPresenter
import kotlinx.android.synthetic.main.contact_fragment.*
import kotlinx.android.synthetic.main.debt_activity.*

class ContactActivity : AppCompatActivity(), AddContactContract.View {

    lateinit var presenter: AddContactPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = AddContactPresenter()
        presenter.attachView(this)
        presenter.viewIsCreated()


        contact_fragment_delete_fab.setOnClickListener { view ->
            presenter.deleteButtonIsPressed()
        }

        val extras = intent.extras
        val contactId = extras?.getInt("contactId")

        Log.d("contactId = $contactId")
        if (contactId == -1) {
            presenter.contactIdIsReceiverFromMainActivity(null)
        } else {
            presenter.contactIdIsReceiverFromMainActivity(contactId.toString())
        }
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
                presenter.addButtonIsPressed()
                return true
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
        Log.d("finishSelf")
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
        Log.d("displaying contact data, alias = $alias, email = $email")
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