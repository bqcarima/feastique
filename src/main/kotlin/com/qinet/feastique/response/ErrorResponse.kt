package com.qinet.feastique.response

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    val code: Int = HttpStatus.INTERNAL_SERVER_ERROR.value(),
    val status: String = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
    val message: String? = null,
    val errors: Map<String, String?>? = null

) {
    companion object {
        fun fromMessage(httpStatus: HttpStatus, message: String?): ErrorResponse {
            return ErrorResponse (
                code = httpStatus.value(),
                status = httpStatus.reasonPhrase,
                message = message
            )
        }

        fun fromErrors(httpStatus: HttpStatus, errors: Map<String, String?>): ErrorResponse {
            return ErrorResponse (
                code = httpStatus.value(),
                status = httpStatus.reasonPhrase,
                errors = errors
            )
        }
    }
}
