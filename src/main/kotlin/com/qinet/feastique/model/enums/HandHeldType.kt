package com.qinet.feastique.model.enums

enum class HandHeldType(val type: String) {
    BURGER("Burger"),
    PIZZA("Pizza"),
    SANDWICH("Sandwich"),
    SHAWARMA("Shawarma"),
    OTHER("Other");

    companion object {
        private val lookup = HandHeldType.entries.associateBy { it.name.uppercase() }
        fun fromString(handheldType: String?): HandHeldType {
            val key = handheldType ?: throw IllegalArgumentException(" null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$handheldType is not a valid entry.")
        }
    }
}

