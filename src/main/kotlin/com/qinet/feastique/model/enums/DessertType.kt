package com.qinet.feastique.model.enums

enum class DessertType(val type: String) {
    CAKE("Cake"),
    ICE_CREAM("Ice Cream"),
    PUDDING("Pudding"),
    PIE("Pie"),
    BROWNIE("Brownie"),
    COOKIE("Cookie"),
    MUFFIN("Muffin"),
    CUPCAKE("Cupcake"),
    DONUT("Donut"),
    OTHER("Other");

    companion object {
        private val lookup = DessertType.entries.associateBy { it.name.uppercase() }
        fun fromString(dessertType: String?): DessertType {
            val key = dessertType ?: throw IllegalArgumentException(" null is not a valid entry.")
            return lookup[key.uppercase()] ?: throw IllegalArgumentException("$dessertType is not a valid entry.")
        }
    }
}

