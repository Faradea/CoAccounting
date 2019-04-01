package com.macgavrina.co_accounting.room

class Calculation {
    var contactAlias: String? = null
    var contactId: Int? = null
    var totalAmount: Double = 0.0
    var currencySymbol: String? = null

    override fun toString(): String {
        return "contactAlias = $contactAlias, contactId = $contactId, totalAmount = $totalAmount"
    }
}