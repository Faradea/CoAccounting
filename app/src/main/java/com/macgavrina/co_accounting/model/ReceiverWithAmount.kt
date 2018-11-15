package com.macgavrina.co_accounting.model

import com.macgavrina.co_accounting.room.Contact

data class ReceiverWithAmount (
        val contact: Contact,
        var amount:Float
)