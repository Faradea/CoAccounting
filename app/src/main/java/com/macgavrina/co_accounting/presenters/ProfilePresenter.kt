package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.interfaces.ProfileContract
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider
import com.macgavrina.co_accounting.rxjava.Events

class ProfilePresenter: BasePresenter<ProfileContract.View>(), ProfileContract.Presenter, UserProvider.LoadUserCallback {

    //Обработка callback от UserProvider().loadUser
    override fun onLoad(user: User) {
        getView()?.hideProgress()
        getView()?.updateUserData(user.login)
    }

    override fun viewIsReady() {
        getView()?.showProgress()
        UserProvider().loadUser(this)
    }

    override fun logoutButtonIsPressed() {
        UserProvider().clearUserData()
        MainApplication.bus.send(Events.LogoutFinished())
    }

}
