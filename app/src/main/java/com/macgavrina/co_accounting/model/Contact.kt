package com.macgavrina.co_accounting.model

data class Contact (
        val ownerId: String,
        val friendId: String,
        val friendEmail: String,
        val alias: String,
        val externalId: String?
)