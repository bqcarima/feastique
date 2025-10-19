package com.qinet.feastique.common.validator.phoneNumber

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneNumberValidator::class])
annotation class ValidPhoneNumber(
    val message: String = "Phone number must start with 6 and be exactly 9 digits long.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

