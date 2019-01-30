package com.macgavrina.co_accounting.interfaces

interface TripContract {

    interface View:BaseViewContract {

        fun getTripTitle():String

        fun getStartDate(): String

        fun getEndDate(): String

        fun getSwitchStatus(): Boolean

        fun showProgress()

        fun hideProgress()

        fun hideKeyboard()

        fun displayToast(text: String)

        fun finishSelf()

        fun displayTripData(title: String?, startDate: String, endDate: String, isCurrent: Boolean)

        fun hideDeleteButton()
    }

    interface Presenter:BasePresenterContract<View> {

        fun doneButtonIsPressed()

        fun deleteButtonIsPressed()

        fun tripIdIsReceiverFromMainActivity(tripId: String?)

    }
}