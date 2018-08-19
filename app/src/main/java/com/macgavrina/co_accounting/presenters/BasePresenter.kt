package com.macgavrina.co_accounting.presenters

import com.macgavrina.co_accounting.interfaces.BasePresenterContract
import com.macgavrina.co_accounting.interfaces.BaseViewContract
import io.reactivex.internal.operators.single.SingleInternalHelper.toObservable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.macgavrina.co_accounting.MainApplication
import com.macgavrina.co_accounting.logging.Log
import io.reactivex.internal.operators.single.SingleInternalHelper.toObservable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.macgavrina.co_accounting.rxjava.Events
import io.reactivex.functions.Consumer
import java.util.logging.Logger


abstract class BasePresenter<T : BaseViewContract> : BasePresenterContract<T> {

    private var view: T? = null

    override fun attachView(baseViewContract: T) {
        view = baseViewContract
    }

    override fun detachView() {
        view = null
    }

    fun getView(): T? {
        return view
    }

    protected fun isViewAttached(): Boolean {
        return view != null
    }

    override fun destroy() {

    }
}