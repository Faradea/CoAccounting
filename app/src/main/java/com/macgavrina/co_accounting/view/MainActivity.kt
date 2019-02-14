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
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.presenters.MainActivityPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainActivityContract.View {

    private var isBackPressed = false
    private var debtFragmentIsDisplayed = false
    lateinit var presenter: MainActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        presenter = MainActivityPresenter()
        presenter.attachView(this)

        //account = CreateSyncAccount(this)!!

/*        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/

        nav_view.getHeaderView(0).nav_header_main_iv.setOnClickListener {view ->
            presenter.headerIsClicked()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        presenter.viewIsCreated()

        nav_view.menu.getItem(1).isChecked = true
        presenter.gotoDebts(true)
    }

    override fun onResume() {
        super.onResume()
        presenter.viewIsReady()
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
        }

        super.onBackPressed()

//        val supportFragmentManager = supportFragmentManager
//        var count = supportFragmentManager.getBackStackEntryCount()
//        if (count > 0) {
//            if (count == 1) {
//
//                if (isBackPressed) {
//                    super.onBackPressed()
//                    super.onBackPressed()
//                } else {
//                    displayDebtsFragment()
//                    nav_view.menu.getItem(1).isChecked = true
//                    Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
//                    isBackPressed = true
//                }
//
//            } else {
//                super.onBackPressed()
//            }
//        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_contacts -> {
                presenter.gotoContactsEvent()
            }
            R.id.nav_debts -> {
                presenter.gotoDebts(false)
            }
            R.id.nav_events -> {
                presenter.gotoTrips()
            }
            R.id.nav_share -> {
                presenter.prepareAndShareData()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun startActivityToShareAllData(dataToShare: String) {

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, dataToShare)
            type = "text/plain"
        }

        startActivity(sendIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun updateLoginText(login: String) {
        nav_view.getHeaderView(0).nav_header_main_tv.text = login
    }

    override fun displayProfileFragment() {
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .add(R.id.content_main_constraint_layout, ProfileFragment())
                .addToBackStack("ProfileFragment")
                .commit()
    }

    override fun displayContactsFragment() {
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .add(R.id.content_main_constraint_layout, ContactsFragment())
                .addToBackStack("ContactsFragment")
                .commit()
    }

    override fun displayTripsFragment() {
        clearStack()
        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .add(R.id.content_main_constraint_layout, TripsFragment())
                .addToBackStack("TripsFragment")
                .commit()
    }

    override fun displayRegisterFragment(enteredLogin: String?) {
        val supportFragmentManager = supportFragmentManager
        val registerFragment = RegisterFragment()
        val bundle = Bundle()
        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
        registerFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
                .add(R.id.content_main_constraint_layout, registerFragment)
                .addToBackStack("RegisterFragment")
                .commit()
    }

    override fun displayEditContactFragment(contactId: String?) {
        clearStack()
        val intent = Intent()
        intent.action = "com.macgavrina.indebt.CONTACT"
        if (contactId == null) {
            intent.putExtra("contactId", -1)
        } else {
            intent.putExtra("contactId", contactId?.toInt())
        }
        startActivity(intent)
    }

    override fun displayDebtsFragment(isInitial: Boolean) {
        clearStack()

        debtFragmentIsDisplayed = true

        if (!isInitial) {
            val supportFragmentManager = supportFragmentManager
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_main_constraint_layout, DebtsFragment())
                    .addToBackStack("DebtsFragment")
                    .commit()
        } else {
            val supportFragmentManager = supportFragmentManager
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content_main_constraint_layout, DebtsFragment())
                    .commit()
        }
    }

    override fun showProgress() {
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, ProgressBarFragment())
//                .commit()
    }

    override fun hideProgress() {
//        clearStack()
//        displayMainFragment()
    }

    override fun hideMenu() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    override fun displayLoginFragment(enteredLogin: String?) {

        clearStack()

        val loginFragment = LoginFragment()
        val bundle:Bundle = Bundle()
        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
        loginFragment.arguments = bundle

        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .add(R.id.content_main_constraint_layout, loginFragment)
                .addToBackStack("LoginFragment")
                .commit()
    }

    override fun displayRecoverPassFragment(enteredLogin: String?) {

        clearStack()

        val recoverPasswordFragment = RecoverPasswordFragment()
        val bundle:Bundle = Bundle()
        bundle.putString(LoginFragment.ENTERED_LOGIN_KEY, enteredLogin)
        recoverPasswordFragment.arguments = bundle

        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .add(R.id.content_main_constraint_layout, recoverPasswordFragment)
                .addToBackStack("RecoverPasswordFragment")
                .commit()
    }

    override fun displayRecoverPassSuccessDialog(title: String, text: String, enteredLogin: String?) {
        clearStack()
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(text)
                .setTitle(title)
        alertDialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
            displayLoginFragment(enteredLogin)
        })
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun displayRegisterSuccessDialog(title: String, text: String) {
        clearStack()
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(text)
                .setTitle(title)
        alertDialogBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, id ->
            displayMainFragment()
        })
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun displayMainFragment() {
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, MainFragment())
//                .commit()
    }

    override fun displayAddContactFragment(contactId: String?) {

        isBackPressed = false

        val intent = Intent()
        intent.action = "com.macgavrina.indebt.CONTACT"
        if (contactId == null) {
            intent.putExtra("contactId", -1)
        } else {
            intent.putExtra("contactId", contactId?.toInt())
        }
        startActivity(intent)
    }

    override fun displayAddTripFragment(tripId: String?) {
        isBackPressed = false

        val intent = Intent()
        intent.action = "com.macgavrina.indebt.TRIP"
        if (tripId == null) {
            intent.putExtra("tripId", -1)
        } else {
            intent.putExtra("tripId", tripId?.toInt())
        }
        startActivity(intent)
    }

    override fun displayAddDebtFragment(debtId: String?) {

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
        if (debtId == null) {
            intent.putExtra("debtId", -1)
        } else {
            intent.putExtra("debtId", debtId?.toInt())
        }
        startActivity(intent)
    }

    override fun displayAddReceiverInAddDebtFragment(debtId: Int, expenseId: Int?) {
//        val addReceiverInAddDebtFragment = ExpenseActivity()
//        val bundle = Bundle()
//        bundle.putInt("debtId", debtId)
//        if (expenseId != null) {
//            bundle.putInt("expenseIdKey", expenseId)
//        }
//        addReceiverInAddDebtFragment.arguments = bundle
//
//        val supportFragmentManager = supportFragmentManager
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.content_main_constraint_layout, addReceiverInAddDebtFragment)
//                .addToBackStack("AddReceiverInDebtFragment")
//                .commit()
    }

    override fun dismissAddReceiverInAddDebtFragment() {
        this.onBackPressed()
    }

    override fun displayAddReceiverInAddDebtFragmentAfterReceiverAdded() {
        this.onBackPressed()
        presenter.addReceiverInAddDebtFragmentAfterReceiverAddedIsDisplayed()
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

    override fun displayToast(text:String) {
        Toast.makeText(MainApplication.applicationContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun displayOnDeleteContactSnackBar() {
        val snackBar = Snackbar.make(content_main_constraint_layout, "Contact is deleted", Snackbar.LENGTH_LONG)
        snackBar!!.setAction("Undo") {
            snackBar?.dismiss()
//            if (main_webview_fragment_webview.canGoBack()) {
//                main_webview_fragment_webview.goBack()
//            } else {
//                main_webview_fragment_webview.loadUrl(MAIN_URL)
//            }
            presenter.undoDeleteContactButtonIsPressed()
        }
        snackBar?.show()
    }

    override fun displayAlert(text: String, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(text)
                .setTitle(title)
                .setPositiveButton("ok") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

}
