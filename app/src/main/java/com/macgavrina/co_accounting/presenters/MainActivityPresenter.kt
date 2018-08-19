package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.MainActivityContract
import com.macgavrina.co_accounting.logging.Log
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.rxjava.Events
import com.macgavrina.co_accounting.rxjava.RxBus


class MainActivityPresenter:BasePresenter<MainActivityContract.View>(), MainActivityContract.Presenter, UserProvider.LoadUserCallback, UserProvider.CheckIfUserTokenExistCallback {

    override fun attachView(baseViewContract: MainActivityContract.View) {
        super.attachView(baseViewContract)

        MainApplication
                .bus
                .toObservable()
                .subscribe { `object` ->
                    when (`object`) {
                        is Events.FromRegisterToLoginEvent -> {
                            getView()?.displayLoginFragment(`object`.myEnteredLogin)
                        }
                        is Events.RegisterIsSuccessful -> {
                            UserProvider().loadUser(this)
                            getView()?.displayRegisterSuccessDialog(`object`.myTitle, `object`.myText)
                        }
                        is Events.RecoverPassIsSuccessful -> {
                            getView()?.displayRecoverPassSuccessDialog(`object`.myTitle, `object`.myText, `object`.myEnteredLogin)
                        }
                        is Events.LogoutFinished -> {
                            UserProvider().loadUser(this)
                            getView()?.displayLoginFragment(null)
                        }
                        is Events.LoginIsSuccessful -> {
                            UserProvider().loadUser(this)
                            getView()?.displayMainFragment()
                        }
                        is Events.FromLoginToRegister -> {
                            getView()?.displayRegisterFragment(`object`.myEnteredLogin)
                        }
                        is Events.FromLoginToRecoverPass -> {
                            getView()?.displayRecoverPassFragment(`object`.myEnteredLogin)
                        }
                    }
                }
    }

    override fun gotoContactsEvent() {
        getView()?.displayContactsFragment()
    }

    override fun viewIsReady() {
        Log.d("MainActivity view id ready")
        UserProvider().loadUser(this)
    }

    override fun headerIsClicked() {

        getView()?.hideMenu()
        getView()?.showProgress()
        UserProvider().checkIfUserTokenExist(this)

    }

    override fun onLoad(user:User) {
        //Выполняется после получения callback с данными о пользователе от UserProvider.CheckIfUserTokenExistCallback
        if (user.login.length != 0) {
            getView()?.updateLoginText(user.login)
        }
        else {
            getView()?.updateLoginText(MainApplication.applicationContext().getString(R.string.default_user_name))
        }
    }

    //Выполняется после получения callback с данными о пользователе от UserProvider.loadUser
    override fun onLoad(ifExist:Boolean) {
            if (ifExist) {
                Log.d("User is already loggined")
                getView()?.hideProgress()
                getView()?.displayProfileFragment()
            } else {
                getView()?.hideProgress()
                getView()?.displayLoginFragment(null)
            }
    }
}