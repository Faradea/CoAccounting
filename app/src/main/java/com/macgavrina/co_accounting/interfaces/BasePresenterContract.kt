package com.macgavrina.co_accounting.interfaces

import android.view.View
import android.icu.lang.UCharacter.GraphemeClusterBreak.V


public interface BasePresenterContract<V : BaseViewContract> {

    fun attachView(baseViewContract: V)

    fun viewIsReady()

    fun detachView()

    fun destroy()


}
