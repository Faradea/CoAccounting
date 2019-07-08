package com.macgavrina.co_accounting.view

import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.support.ADD_CONTACT_REQUEST_CODE
import com.macgavrina.co_accounting.support.GO_TO_CONTACTS_RESULT_CODE
import com.macgavrina.co_accounting.support.GO_TO_CURRENT_TRIP_RESULT_CODE
import com.macgavrina.co_accounting.viewmodel.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var isBackPressed = false
    private var debtFragmentIsDisplayed = false
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //Crash app to test Crashlytics
//        var nullInt: Int? = null
//        nullInt!!


        nav_view.getHeaderView(0).nav_header_main_iv.setOnClickListener {view ->
            //presenter.headerIsClicked()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        viewModel.getCheckedNavMenuItemPosition().observe(this, Observer { menuItem ->
            if (menuItem != -1) {
                nav_view.menu.getItem(menuItem).isChecked = true
            }
        })

        viewModel.getDisplayedFragmentId().observe(this, Observer { fragmentId ->
            when (fragmentId) {
                CONTACTS_FRAGMENT_ID -> displayContactsFragment()
                DEBTS_FRAGMENT_ID -> displayDebtsFragment(false)
                TRIPS_FRAGMENT_ID -> displayTripsFragment()
                CALCULATIONS_FRAGMENT_ID -> displayCalculationsFragment()
            }
        })

        viewModel.goToTrip.observe(this, Observer { tripId ->
            displayTripFragment(tripId)
        })

        viewModel.goToContact.observe(this, Observer { contactId ->
            displayAddContactFragment(contactId)
        })

        viewModel.goToDebt.observe(this, Observer { debtId ->
            displayAddDebtFragment(debtId)
        })

        viewModel.goToDebtAsInitialScreen.observe(this, Observer {
            displayDebtsFragment(true)
        })
    }

    override fun onBackPressed() {

        //ToDo REFACT Сделать нормально
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        }

        if (debtFragmentIsDisplayed && !isBackPressed) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            isBackPressed = true
            return
        }

        if (!debtFragmentIsDisplayed) {
            debtFragmentIsDisplayed = true
            nav_view.menu.getItem(1).isChecked = true
            title = resources.getString(R.string.debts_actionbar_title)
        }

        super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_contacts -> {
                viewModel.navMenuItemIsChecked(CONTACTS_NAV_MENU_ITEM_POSITION)
            }
            R.id.nav_debts -> {
                viewModel.navMenuItemIsChecked(DEBTS_NAV_MENU_ITEM_POSITION)
            }
            R.id.nav_events -> {
                viewModel.navMenuItemIsChecked(TRIPS_NAV_MENU_ITEM_POSITION)
            }
            R.id.nav_calculations -> {
                viewModel.navMenuItemIsChecked(CALCULATIONS_MENU_ITEM_POSITION)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("onActivityResult, requestCode = $requestCode, resultCode = $resultCode")
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ADD_CONTACT_REQUEST_CODE -> {
                if (resultCode == GO_TO_CONTACTS_RESULT_CODE) {
                    viewModel.goToContactsFromDebtRequest()
                }
                if (resultCode == GO_TO_CURRENT_TRIP_RESULT_CODE) {
                    viewModel.goToCurrentTripFromDebtRequest()
                }
            }
        }
    }

//    private fun updateLoginText(login: String) {
//        nav_view.getHeaderView(0).nav_header_main_tv.text = login
//    }

//    private fun displayProfileFragment() {
//        title = resources.getString(R.string.app_name)
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, ProfileFragment())
//                .addToBackStack("ProfileFragment")
//                .commit()
//    }

    private fun displayContactsFragment() {
        title = resources.getString(R.string.contacts_actionbar_title)
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, ContactsFragment())
                .addToBackStack("ContactsFragment")
                .commit()
    }

    private fun displayTripsFragment() {
        title = resources.getString(R.string.trips_actionbar_title)
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, TripsFragment())
                .addToBackStack("TripsFragment")
                .commit()
    }

    private fun displayCalculationsFragment() {
        title = resources.getString(R.string.calculations_actionbar_title)
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, CalculationsFragment())
                .addToBackStack("CalculationsFragment")
                .commit()
    }

//    private fun displayRegisterFragment(enteredLogin: String?) {
//        title = resources.getString(R.string.app_name)
//        val supportFragmentManager = supportFragmentManager
//        val registerFragment = RegisterFragment()
//        val bundle = Bundle()
//        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
//        registerFragment.arguments = bundle
//        supportFragmentManager.beginTransaction()
//                .add(R.id.content_main_constraint_layout, registerFragment)
//                .addToBackStack("RegisterFragment")
//                .commit()
//    }

//    override fun displayEditContactFragment(contactId: String?) {
//        clearStack()
//        val intent = Intent()
//        intent.action = "com.macgavrina.indebt.CONTACT"
//        if (contactId == null) {
//            intent.putExtra("contactId", -1)
//        } else {
//            intent.putExtra("contactId", contactId?.toInt())
//        }
//        startActivity(intent)
//    }

    private fun displayDebtsFragment(isInitial: Boolean) {
        title = resources.getString(R.string.debts_actionbar_title)
        clearStack()

        debtFragmentIsDisplayed = true

        if (!isInitial) {
            val supportFragmentManager = supportFragmentManager
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_main_constraint_layout, DebtsFragmentMVVM())
                    .addToBackStack("DebtsFragment")
                    .commit()
        } else {
            val supportFragmentManager = supportFragmentManager
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content_main_constraint_layout, DebtsFragmentMVVM())
                    .commit()
        }
    }

//    override fun showProgress() {
////        val supportFragmentManager = supportFragmentManager
////        supportFragmentManager.beginTransaction()
////                .replace(R.id.content_main_constraint_layout, ProgressBarFragment())
////                .commit()
//    }
//
//    override fun hideProgress() {
////        clearStack()
////        displayMainFragment()
//    }

    private fun hideMenu() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

//    private fun displayLoginFragment(enteredLogin: String?) {
//        title = resources.getString(R.string.app_name)
//        clearStack()
//
//        val loginFragment = LoginFragment()
//        val bundle:Bundle = Bundle()
//        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
//        loginFragment.arguments = bundle
//
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .add(R.id.content_main_constraint_layout, loginFragment)
//                .addToBackStack("LoginFragment")
//                .commit()
//    }

//    override fun displayRecoverPassFragment(enteredLogin: String?) {
//
//        clearStack()
//
//        val recoverPasswordFragment = RecoverPasswordFragment()
//        val bundle:Bundle = Bundle()
//        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
//        recoverPasswordFragment.arguments = bundle
//
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .add(R.id.content_main_constraint_layout, recoverPasswordFragment)
//                .addToBackStack("RecoverPasswordFragment")
//                .commit()
//    }

//    override fun displayRecoverPassSuccessDialog(title: String, text: String, enteredLogin: String?) {
//        clearStack()
//        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
//        alertDialogBuilder.setMessage(text)
//                .setTitle(title)
//        alertDialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
//            displayLoginFragment(enteredLogin)
//        })
//        val alertDialog: AlertDialog = alertDialogBuilder.create()
//        alertDialog.show()
//    }
//
//    override fun displayRegisterSuccessDialog(title: String, text: String) {
//        clearStack()
//        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
//        alertDialogBuilder.setMessage(text)
//                .setTitle(title)
//        alertDialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
//            displayMainFragment()
//        })
//        val alertDialog: AlertDialog = alertDialogBuilder.create()
//        alertDialog.show()
//    }

    private fun displayAddContactFragment(contactId: Int) {
        isBackPressed = false

        val intent = Intent()
        intent.action = "com.macgavrina.indebt.CONTACT"
        intent.putExtra("contactId", contactId)
        startActivity(intent)
    }

    private fun displayAddTripFragment(tripId: Int) {
        isBackPressed = false

        val intent = Intent()
        intent.action = "com.macgavrina.indebt.TRIP"
        intent.putExtra("tripId", tripId?.toInt())
        startActivity(intent)
    }

    private fun displayTripFragment(tripId: Int) {
        isBackPressed = false

        val intent = Intent()
        intent.action = "com.macgavrina.indebt.TRIP"
        intent.putExtra("tripId", tripId)
        startActivity(intent)
    }

    private fun displayAddDebtFragment(debtId: Int) {

        isBackPressed = false
//        val addDebtFragment = AddDebtFragment()
//
//        if (debtId != null) {
//            val bundle = Bundle()
//            bundle.putInt(AddReceiverInAddDebtFragment.DEBT_ID_KEY, debtId.toInt())
//            addDebtFragment.arguments = bundle
//        }
//
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, addDebtFragment)
//                .addToBackStack("AddDebtFragment")
//                .commit()


        val intent = Intent()
        intent.action = "com.macgavrina.indebt.DEBT"
        intent.putExtra("debtId", debtId)
        startActivityForResult(intent, ADD_CONTACT_REQUEST_CODE)
    }

    private fun clearStack() {
        isBackPressed = false
        debtFragmentIsDisplayed = false
        val supportFragmentManager = supportFragmentManager
        var count = supportFragmentManager.getBackStackEntryCount()
        while (count >= 0) {
            supportFragmentManager.popBackStack()
            count--
        }
    }

    /*
    fun CreateSyncAccount(context: Context): Account? {
        // Create the account type and default account
        val newAccount = Account(SyncService.ACCOUNT, SyncService.ACCOUNT_TYPE)
        // Get an instance of the Android account manager
        val accountManager = context.getSystemService(
                Context.ACCOUNT_SERVICE) as AccountManager
        *//*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         *//*
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            *//*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             *//*
            Log.d("account is initialized")
            return newAccount
        } else {
            *//*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             *//*
            Log.d("account initializing error")
            return newAccount
        }
    }*/

    private fun displayToast(text:String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

//    override fun displayOnDeleteContactSnackBar() {
//        val snackBar = Snackbar.make(content_main_constraint_layout, "Contact is deleted", Snackbar.LENGTH_LONG)
//        snackBar!!.setAction("Undo") {
//            snackBar?.dismiss()
////            if (main_webview_fragment_webview.canGoBack()) {
////                main_webview_fragment_webview.goBack()
////            } else {
////                main_webview_fragment_webview.loadUrl(MAIN_URL)
////            }
//            presenter.undoDeleteContactButtonIsPressed()
//        }
//        snackBar?.show()
//    }

    private fun displayAlert(text: String, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(text)
                .setTitle(title)
                .setPositiveButton("ok") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

}
