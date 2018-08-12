package com.macgavrina.co_accounting.interfaces

interface BasePresenterContract<V : BaseViewContract> {

    fun attachView(baseViewContract: V)

    fun viewIsReady()

    fun detachView()

    fun destroy()

}
