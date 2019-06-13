package com.macgavrina.co_accounting.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.room.Contact
import com.macgavrina.co_accounting.viewmodel.ContactsViewModel
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.contact_fragment.*

class ContactActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactsViewModel
    private var contactId: Int = -1
    private var contact: Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        setSupportActionBar(toolbar)

        hideProgress()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(ContactsViewModel::class.java)

        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                displayToast(res)
            }
        })

        //ToDo REFACT Использовать фрагмент вместо activity и this.viewLifecycleOwner вместо this для всех observe
        viewModel.getAllContactsForCurrentTrip().observe(this, Observer<List<Contact>> {})

        val extras = intent.extras
        if (extras?.getInt("contactId") != null && extras.getInt("contactId") != -1) {
            contactId = extras.getInt("contactId")
            viewModel.getContactById(contactId.toString())
                    .observe(this,
                            Observer<Contact> { contact ->
                                this.contact = contact
                                contact_fragment_name_et.setText(contact.alias)
                                contact_fragment_email_et.setText(contact.email)
                            })
        } else {
            hideDeleteButton()
        }


        contact_fragment_delete_fab.setOnClickListener { view ->
            if (contact != null) {
                viewModel.safeDeleteContact(contact!!)
            }
            finishSelf()
        }

//        contact_fragment_email_et.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable) {}
//
//            override fun beforeTextChanged(s: CharSequence, start: Int,
//                                           count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int,
//                                       before: Int, count: Int) {
//                if (s.isEmpty()) {
//                    contact_fragment_email_til.error = "Enter email"
//                } else {
//                    contact_fragment_email_til.error = null
//                }
//            }
//        })

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_menu_done -> {

                if (!getAlias().isNullOrEmpty()) {

                    if (contact == null) {
                        contact = Contact()
                    }

                    contact!!.email = getEmail()
                    contact!!.alias = getAlias()

                    if (contactId == -1) {
                        viewModel.insertContact(contact!!)
                        finishSelf()
                    } else {
                        contact!!.uid = contactId
                        viewModel.updateContact(contact!!)
                        finishSelf()
                    }

                    return true
                } else {
                    contact_fragment_name_til.error = "Enter name"
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewIsDestroyed()
    }

    private fun hideKeyboard() {
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    private fun displayToast(text: String) {
        Log.d("Display toast with text = $text")
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun finishSelf() {
        onBackPressed()
    }

    private fun getAlias(): String {
        return contact_fragment_name_et.text.toString()
    }

    private fun getEmail(): String {
        return contact_fragment_email_et.text.toString()
    }

    private fun showProgress() {
        contact_fragment_progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        contact_fragment_progressBar.visibility = View.INVISIBLE
    }

    private fun hideDeleteButton() {
        contact_fragment_delete_fab.hide()
    }

//    private fun displayAlert(text: String, title: String) {
//        val builder = AlertDialog.Builder(this)
//        builder.setMessage(text)
//                .setTitle(title)
//                .setPositiveButton("ok") { _, _ -> }
//        val dialog = builder.create()
//        dialog.show()
//    }

}