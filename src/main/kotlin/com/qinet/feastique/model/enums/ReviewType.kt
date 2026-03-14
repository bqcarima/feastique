package com.qinet.feastique.model.enums

enum class ReviewType(val type: String) {
    BEVERAGE("Beverage"),
    DESSERT("Dessert"),
    FOOD("Food"),
    HANDHELD("Handheld"),
    VENDOR("Vendor");

    companion object {
        private val lookup = ReviewType.entries.associateBy { it.name.uppercase() }
        fun fromString(reviewType: String?): ReviewType {
            val key = reviewType ?: throw IllegalArgumentException(" null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$reviewType is not a valid entry.")
        }
    }
}