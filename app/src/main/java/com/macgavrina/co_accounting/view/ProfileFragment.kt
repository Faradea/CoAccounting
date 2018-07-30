package com.macgavrina.co_accounting.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.R
import com.macgavrina.co_accounting.interfaces.ProfileContract
import com.macgavrina.co_accounting.presenters.ProfilePresenter
import kotlinx.android.synthetic.main.profile_fragment.*
import android.app.Activity
import android.content.Context


class ProfileFragment: Fragment(), ProfileContract.View {

    lateinit var profilePresenter: ProfilePresenter

    interface OnLogoutFinishedListener {
        fun logoutFinished()
    }

    lateinit var onLogoutFinishedListener: OnLogoutFinishedListener

    override fun updateUserData(login: String?) {
        profile_fragment_login_tv.text = login
    }

    override fun finishSelf() {
        onLogoutFinishedListener.logoutFinished()
    }

    override fun showProgress() {
        profile_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        profile_fragment_progress_bar.visibility = View.INVISIBLE
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            onLogoutFinishedListener = activity as OnLogoutFinishedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement onLogoutFinishedListener")
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        profilePresenter = ProfilePresenter()
        profilePresenter.attachView(this)

        return inflater.inflate(R.layout.profile_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profilePresenter.viewIsReady()

        profile_fragment_logout_tv.setOnClickListener { view ->
            profilePresenter.logoutButtonIsPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profilePresenter.detachView()
    }

}