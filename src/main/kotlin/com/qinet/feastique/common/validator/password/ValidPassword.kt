package com.qinet.feastique.common.validator.password

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class ValidPassword(
    val message: String = "Password must be at least 8 characters long, contain at least one uppercase letter and one number.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

