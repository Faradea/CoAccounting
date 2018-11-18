package com.macgavrina.co_accounting.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.macgavrina.co_accounting.R

class ProgressBarFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.progress_bar_fragment, container,
                false)
    }
}