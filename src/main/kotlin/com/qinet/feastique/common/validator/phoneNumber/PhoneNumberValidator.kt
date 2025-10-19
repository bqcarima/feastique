package com.qinet.feastique.common.validator.phoneNumber

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PhoneNumberValidator : ConstraintValidator<ValidPhoneNumber, String?> {

    private val regex = Regex("^6\\d{8}$")

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        return regex.matches(value.trim())
    }
}

