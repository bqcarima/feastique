package com.qinet.feastique.common.validator.password

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PinValidator : ConstraintValidator<ValidPin, String> {
    private val pinRegex = Regex("^\\d{4}$")
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        return pinRegex.matches(value)
    }
}