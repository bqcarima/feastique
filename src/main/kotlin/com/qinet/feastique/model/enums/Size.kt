package com.qinet.feastique.model.enums

enum class Size(val type: String) {
    EXTRA_LARGE("Extra Large"),
    LARGE("Large"),
    MEDIUM("Medium");

    companion object {
        private val lookup = Size.entries.associateBy { it.name.uppercase() }
        fun fromString(sizeName: String): Size =
            lookup[sizeName.uppercase()] ?: throw IllegalArgumentException("$sizeName is not a valid entry.")
    }
}

