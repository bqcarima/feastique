package com.qinet.feastique.common.validator.username

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UsernameValidator::class])
annotation class ValidUsername(
    val message: String = "Username must be 4–15 characters long, cannot start with a number, and may only contain letters, numbers, or underscores.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

