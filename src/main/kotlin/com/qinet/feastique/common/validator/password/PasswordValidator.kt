package com.qinet.feastique.common.validator.password

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PasswordValidator : ConstraintValidator<ValidPassword, String> {

    private val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        return passwordRegex.matches(value)
    }
}

