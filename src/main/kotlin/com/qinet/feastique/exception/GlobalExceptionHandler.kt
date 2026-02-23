package com.qinet.feastique.exception

import com.qinet.feastique.response.ErrorResponse
import jakarta.annotation.PostConstruct
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
@Component
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    @PostConstruct
    fun init() {
        println("GlobalExceptionHandler initialized!")
    }

    @ExceptionHandler(RequestedEntityNotFoundException::class)
    fun handleRequestedEntityNotFoundException(e: RequestedEntityNotFoundException): ResponseEntity<ErrorResponse> {

        val status = HttpStatus.NOT_FOUND
        val message = "Entity not found."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }


    @ExceptionHandler(DuplicateFoundException::class)
    fun handleDuplicateFoundException(e: DuplicateFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        val message = "Cannot add duplicate with the same name."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        val message = "Please make sure all fields are filled properly."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(MalformedUrlException::class)
    fun handleMalformedUrlException(e: MalformedUrlException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_GATEWAY
        val message = "Malformed URL, cannot process request."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(MultipleRolesException::class)
    fun handleMultipleRolesException(e: MultipleRolesException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.MULTI_STATUS
        val message = "Multiple roles forbidden."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(PermissionDeniedException::class)
    fun handlePermissionDeniedException(e: PermissionDeniedException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNAUTHORIZED
        val message = "You do not have the permission to perform operation."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(PhoneNumberNotFoundException::class)
    fun handlePhoneNumberNotFoundException(e: PhoneNumberNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        val message = "The phone number not found."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(PhoneNumberUnavailableException::class)
    fun handlePhoneNumberUnavailableException(e: PhoneNumberUnavailableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        val message =
            "The phone number is already associated with a different account. Contact customer support to claim."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUserNotFoundException(e: UsernameNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        val message = "User not found."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUncategorizedException(e: Exception): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val genericErrorMessage = "An unexpected error occurred. Please contact customer support. ${e.message}"
        return ResponseEntity(
            ErrorResponse.fromMessage(status, genericErrorMessage),
            status
        )
    }

    @ExceptionHandler(UsernameUnavailableException::class)
    fun handleUserNameUnavailableException(e: UsernameUnavailableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        val message = "Username is already taken."

        return ResponseEntity(
            ErrorResponse.fromMessage(
                status,
                e.message ?: message
            ),
            status
        )
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<in Any>? {
        val status = HttpStatus.BAD_REQUEST
        val errors = ex.bindingResult.allErrors
            .filterIsInstance<FieldError>()
            .associate { it.field to it.defaultMessage }

        return ResponseEntity(
            ErrorResponse.fromErrors(status, errors),
            status
        )
    }
}

