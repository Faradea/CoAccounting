package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.presenters.MainActivityPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainActivityContract.View {

    override fun showProgress() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideProgress() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    lateinit var mainActivityPresenter: MainActivityPresenter

    override fun hideMenu() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    override fun displayLoginFragment() {

        val supportFragmentManager = supportFragmentManager
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_main_constraint_layout, LoginFragment())
                //ToDo Продумать как не добавлять пачку одинаковых фрагментов в стек
                .addToBackStack("loginFragment")
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initView()

        mainActivityPresenter = MainActivityPresenter()
        mainActivityPresenter.attachView(this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        nav_view.getHeaderView(0).nav_header_main_iv.setOnClickListener {view ->
            mainActivityPresenter.headerIsClicked()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    //ToDo Менять имя и иконку для авторизованного пользователя
/*    override fun onResume() {
        super.onResume()
        nav_view.getHeaderView(0).nav_header_main_tv.text = "?"
    }*/

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityPresenter.detachView()
    }

    fun initView() {
        //ToDo Здесь должно быть DI или что-то типа того, например:
        //ButterKnife.bind(this);
    }
}
