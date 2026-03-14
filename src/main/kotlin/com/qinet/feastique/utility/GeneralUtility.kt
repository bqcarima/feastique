package com.qinet.feastique.utility

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

/**
 * General utility class for common functions that can be used across the application.
 * Currently, includes an OrderId generator that creates unique order IDs based
 * on the current date/time and a random component.
 */
class GeneralUtility {

    /**
     * Data class representing an Order ID, consisting of an
     * internal order ID and a user-friendly order code.
     * The internal order ID is a combination of the current date/time and a random component,
     * while the user order code is a random numeric string.
     */
    data class OrderId(
        val internalOrderId: String,
        val userOrderCode: String
    )

    /**
     * Object responsible for generating unique Order IDs.
     * It uses the current date/time formatted as "yyyyMMdd-HHmmSS" and a random 7-digit number
     * to create a unique internal order ID and a user-friendly order code.
     */
    object OrderIdGenerator {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        fun generate(): OrderId {
            val dateTime = LocalDateTime.now().format(dateTimeFormatter)
            val randomComponent = Random.nextInt(1000000, 9999999).toString()
            val internalOrderId = "$dateTime-$randomComponent"
            return OrderId(internalOrderId, randomComponent)
        }
    }
}

/**
 * Extension function to convert a Date object to a
 * LocalDate object using the system default time zone.
 */
fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

