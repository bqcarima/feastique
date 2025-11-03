package com.qinet.feastique.model.enums

enum class BeverageGroup(val type: String) {

    BEER("Beer"),
    CHAMPAGNE("Champagne"),
    COCKTAIL("Cocktail"),
    JUICE("Juice"),
    DAIRY("Dairy"),
    MOCKTAIL("Mocktail"),
    SMOOTHIES("Smoothies"),
    SOFT_DRINK("Soft drink"),
    WATER("Water"),
    WHISKEY("Whiskey"),
    WINE("Wine"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): BeverageGroup {
            if (value.isNullOrBlank()) return OTHER
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                        it.type.equals(value, ignoreCase = true) ||
                        it.type.replace(" ", "_").equals(value, ignoreCase = true)
            } ?: OTHER
        }
    }
}

