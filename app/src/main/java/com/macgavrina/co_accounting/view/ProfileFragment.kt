package com.macgavrina.co_accounting.view

import android.os.Bundle
import androidx.fragment.app.Fragment
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

    lateinit var presenter: ProfilePresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        presenter = ProfilePresenter()
        presenter.attachView(this)

        return inflater.inflate(R.layout.profile_fragment, container,
                false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.viewIsReady()

        profile_fragment_logout_tv.setOnClickListener { view ->
            presenter.logoutButtonIsPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun updateUserData(login: String?) {
        profile_fragment_login_tv.text = login
    }

    override fun showProgress() {
        profile_fragment_progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        profile_fragment_progress_bar.visibility = View.INVISIBLE
    }

}