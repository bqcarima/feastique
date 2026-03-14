package com.qinet.feastique.exception

import com.qinet.feastique.response.error.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(RequestedEntityNotFoundException::class)
    fun handleRequestedEntityNotFoundException(e: RequestedEntityNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Entity not found."), status)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "User not found."), status)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(e: UsernameNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNAUTHORIZED
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "User not found."), status)
    }

    @ExceptionHandler(DuplicateFoundException::class)
    fun handleDuplicateFoundException(e: DuplicateFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Cannot add duplicate with the same name."), status)
    }

    @ExceptionHandler(UsernameUnavailableException::class)
    fun handleUsernameUnavailableException(e: UsernameUnavailableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Username is already taken."), status)
    }

    @ExceptionHandler(PhoneNumberUnavailableException::class)
    fun handlePhoneNumberUnavailableException(e: PhoneNumberUnavailableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "The phone number is already associated with a different account."), status)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Please make sure all fields are filled properly."), status)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Conflict occurred."), status)
    }

    @ExceptionHandler(PermissionDeniedException::class)
    fun handlePermissionDeniedException(e: PermissionDeniedException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.FORBIDDEN
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "You do not have permission to perform this operation."), status)
    }

    @ExceptionHandler(NoRoleException::class)
    fun handleNoRoleException(e: NoRoleException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.FORBIDDEN
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "User has no role."), status)
    }

    @ExceptionHandler(MultipleRolesException::class)
    fun handleMultipleRolesException(e: MultipleRolesException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.FORBIDDEN
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Multiple roles forbidden."), status)
    }

    @ExceptionHandler(EntityNotAvailableException::class)
    fun handleEntityNotAvailableException(e: EntityNotAvailableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Item is not available."), status)
    }

    @ExceptionHandler(EntityNotDeliverableException::class)
    fun handleEntityNotDeliverableException(e: EntityNotDeliverableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Item is not deliverable."), status)
    }

    @ExceptionHandler(MalformedUrlException::class)
    fun handleMalformedUrlException(e: MalformedUrlException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_GATEWAY
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "Malformed URL, cannot process request."), status)
    }

    @ExceptionHandler(PhoneNumberNotFoundException::class)
    fun handlePhoneNumberNotFoundException(e: PhoneNumberNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        return ResponseEntity(ErrorResponse.fromMessage(status, e.message ?: "The phone number was not found."), status)
    }

    @ExceptionHandler(Exception::class)
    fun handleUncategorizedException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception: ", e)
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        return ResponseEntity(
            ErrorResponse.fromMessage(status, "An unexpected error occurred. Please contact customer support."),
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

        return ResponseEntity(ErrorResponse.fromErrors(status, errors), status)
    }
}