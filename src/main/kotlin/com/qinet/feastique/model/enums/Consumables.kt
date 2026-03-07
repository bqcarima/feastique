package com.qinet.feastique.model.enums

enum class Consumables(val type: String) {
    BEVERAGE("Beverage"),
    DESSERT("Dessert"),
    FOOD("Food"),
    HANDHELD("Handheld");

    companion object {
        private val lookup = Consumables.entries.associateBy { it.name.uppercase() }
        fun fromString(consumable: String?): Consumables {
            val key = consumable ?: throw IllegalArgumentException("null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$consumable is not a valid entry.")
        }
    }
}