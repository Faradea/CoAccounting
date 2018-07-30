package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.ProfileContract
import com.macgavrina.co_accounting.model.User
import com.macgavrina.co_accounting.providers.UserProvider

class ProfilePresenter: BasePresenter<ProfileContract.View>(), ProfileContract.Presenter, UserProvider.LoadUserCallback {

    override fun onLoad(user: User) {
        getView()!!.hideProgress()
        getView()!!.updateUserData(user.login)
    }

    override fun viewIsReady() {
        getView()!!.showProgress()
        UserProvider().loadUser(this)
    }

    override fun logoutButtonIsPressed() {
        UserProvider().clearUserData()
        getView()!!.finishSelf()
    }

}
