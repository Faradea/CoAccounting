package com.macgavrina.co_accounting.interfaces

interface TripContract {

    interface View:BaseViewContract {

        fun getTripTitle():String

        fun getStartDate(): Long?

        fun getEndDate(): Long?

        fun getSwitchStatus(): Boolean

        fun showProgress()

        fun hideProgress()

        fun hideKeyboard()

        fun displayToast(text: String)

        fun finishSelf()

        fun displayTripData(title: String?, startDate: Long?, endDate: Long?, isCurrent: Boolean)

        fun hideDeleteButton()

        fun displayAlertDialog(text: String)
    }

    interface Presenter:BasePresenterContract<View> {

        fun doneButtonIsPressed()

        fun deleteButtonIsPressed()

        fun tripIdIsReceiverFromMainActivity(tripId: String?)

    }
}