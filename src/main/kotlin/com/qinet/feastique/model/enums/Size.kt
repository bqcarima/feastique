package com.qinet.feastique.model.enums

enum class Size(val type: String) {
    EXTRA_LARGE("Extra Large"),
    LARGE("Large"),
    MEDIUM("Medium"),
    SMALL("Small");

    companion object {
        private val lookup = Size.entries.associateBy { it.name.uppercase() }
        fun fromString(size: String? ): Size {
            val key = size ?: throw IllegalArgumentException(" null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$size is not a valid entry.")
        }
    }
}

