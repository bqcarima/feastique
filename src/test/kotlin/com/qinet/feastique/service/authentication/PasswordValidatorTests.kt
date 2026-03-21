package com.qinet.feastique.service.authentication


import com.qinet.feastique.common.validator.password.PasswordValidator
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class PasswordValidatorTest {

    private lateinit var validator: PasswordValidator
    private lateinit var context: ConstraintValidatorContext

    @BeforeEach
    fun setUp() {
        validator = PasswordValidator()
        context   = mock()
    }


    // Valid passwords
    @Nested
    inner class ValidPasswords {

        @Test
        fun `passWord123 is valid`() {
            assertTrue(validator.isValid("passWord123", context))
        }

        @Test
        fun `sabiChef98 is valid`() {
            assertTrue(validator.isValid("sabiChef98", context))
        }

        @Test
        fun `exactly 8 characters with one uppercase and one digit is valid`() {
            assertTrue(validator.isValid("Abcdef1!", context))
        }

        @Test
        fun `long password with uppercase and digit is valid`() {
            assertTrue(validator.isValid("ThisIsAVeryLongPassword1", context))
        }

        @Test
        fun `password with multiple uppercase letters and digits is valid`() {
            assertTrue(validator.isValid("ABCdef123", context))
        }

        @Test
        fun `password with special characters uppercase and digit is valid`() {
            assertTrue(validator.isValid("Hello@World1", context))
        }

        @Test
        fun `password with digit at the start is valid`() {
            assertTrue(validator.isValid("1UpperCase", context))
        }

        @Test
        fun `password with uppercase at the end is valid`() {
            assertTrue(validator.isValid("lowercase1A", context))
        }
    }


    // Invalid — too short
    @Nested
    inner class TooShort {

        @Test
        fun `7 characters with uppercase and digit is invalid`() {
            assertFalse(validator.isValid("Pass1ab", context))
        }

        @Test
        fun `single character is invalid`() {
            assertFalse(validator.isValid("A", context))
        }

        @Test
        fun `empty string is invalid`() {
            assertFalse(validator.isValid("", context))
        }

        @Test
        fun `null is invalid`() {
            assertFalse(validator.isValid(null, context))
        }
    }


    // Invalid — missing uppercase
    @Nested
    inner class MissingUppercase {

        @Test
        fun `all lowercase with digit is invalid`() {
            assertFalse(validator.isValid("password1", context))
        }

        @Test
        fun `lowercase with special characters and digit but no uppercase is invalid`() {
            assertFalse(validator.isValid("p@ssword1", context))
        }

        @Test
        fun `all digits no uppercase is invalid`() {
            assertFalse(validator.isValid("12345678", context))
        }
    }


    // Invalid — missing digit
    @Nested
    inner class MissingDigit {

        @Test
        fun `all uppercase letters is invalid`() {
            assertFalse(validator.isValid("PASSWORD", context))
        }

        @Test
        fun `mixed case letters without digit is invalid`() {
            assertFalse(validator.isValid("PassWord!", context))
        }

        @Test
        fun `8 characters with uppercase but no digit is invalid`() {
            assertFalse(validator.isValid("Abcdefgh", context))
        }
    }


    // Invalid — missing both uppercase and digit
    @Nested
    inner class MissingUppercaseAndDigit {

        @Test
        fun `all lowercase no digit is invalid`() {
            assertFalse(validator.isValid("password", context))
        }

        @Test
        fun `lowercase with special characters only is invalid`() {
            assertFalse(validator.isValid("p@ssword!", context))
        }
    }
}

