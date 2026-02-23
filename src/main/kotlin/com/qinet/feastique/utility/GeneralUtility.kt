package com.qinet.feastique.utility

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random


class GeneralUtility {

    // Generating order ids
    data class OrderId(
        val internalOrderId: String,
        val userOrderCode: String
    )

    object OrderIdGenerator {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmSS")

        fun generate(): OrderId {
            val dateTime = LocalDateTime.now().format(dateTimeFormatter)
            val randomComponent = Random.nextInt(1000000, 9999999).toString()
            val internalOrderId = "$dateTime-$randomComponent"
            return OrderId(internalOrderId, randomComponent)
        }
    }
}
fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

