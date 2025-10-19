package com.qinet.feastique.common.validator.username

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class UsernameValidator : ConstraintValidator<ValidUsername, String> {
    private val usernameRegex = Regex("^(?!\\d)[A-Za-z0-9_]{4,15}$")

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        return usernameRegex.matches(value)
    }
}

