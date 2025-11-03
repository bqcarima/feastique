package com.qinet.feastique.model.enums

enum class AccountType(val type: String) {
    CUSTOMER("Customer"),
    VENDOR("Customer");

    companion object {
        private val lookup = AccountType.entries.associateBy { it.name.uppercase() }
        fun fromString(accountTypeName: String): AccountType =
            lookup[accountTypeName.uppercase()] ?: throw IllegalArgumentException("$accountTypeName is not a valid entry.")
    }
}

