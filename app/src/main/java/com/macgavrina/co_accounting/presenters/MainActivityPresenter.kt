package com.macgavrina.co_accounting.presenters

import android.util.Log
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.preferences.MySharedPreferences
import com.macgavrina.co_accounting.providers.LoadUsersTask
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider.LoadUserCallback



class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback {

    override fun viewIsReady() {
    }

    override fun headerIsClicked() {

        getView()!!.hideMenu()

        getView()!!.showProgress()
        UserProvider().loadUser(this)
        getView()!!.hideProgress()

    }

    //Выполняется после получения callback с данными о пользователе от UserProvider
    override fun onLoad(user: User) {
        if (user.token.length !=0 ) {
            Log.d("InDebtApp", "User is already logined, token = ${user.token}")
        } else {
            getView()!!.displayLoginFragment()
        }
    }
}