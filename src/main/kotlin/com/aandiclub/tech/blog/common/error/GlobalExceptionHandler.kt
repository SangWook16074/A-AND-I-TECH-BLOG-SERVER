package com.aandiclub.tech.blog.common.error

import com.aandiclub.tech.blog.common.filter.CorrelationIdFilter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException::class)
	fun handleResponseStatusException(
		exception: ResponseStatusException,
		exchange: ServerWebExchange,
	): ResponseEntity<ErrorResponse> {
		val status = exception.statusCode
		return ResponseEntity.status(status).body(
			ErrorResponse(
				code = resolveCode(status.value(), (status as? HttpStatus)?.name),
				message = exception.reason ?: status.toString(),
				timestamp = Instant.now(),
				path = exchange.request.path.value(),
				traceId = traceId(exchange),
			),
		)
	}

	@ExceptionHandler(WebExchangeBindException::class)
	fun handleValidationException(
		exception: WebExchangeBindException,
		exchange: ServerWebExchange,
	): ResponseEntity<ErrorResponse> {
		val message = exception.fieldErrors
			.takeIf { it.isNotEmpty() }
			?.joinToString("; ") { it.asMessage() }
			?: "validation failed"

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
			ErrorResponse(
				code = "VALIDATION_FAILED",
				message = message,
				timestamp = Instant.now(),
				path = exchange.request.path.value(),
				traceId = traceId(exchange),
			),
		)
	}

	@ExceptionHandler(Exception::class)
	fun handleUnhandledException(
		exception: Exception,
		exchange: ServerWebExchange,
	): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
			ErrorResponse(
				code = "INTERNAL_SERVER_ERROR",
				message = exception.message ?: "internal server error",
				timestamp = Instant.now(),
				path = exchange.request.path.value(),
				traceId = traceId(exchange),
			),
		)

	private fun traceId(exchange: ServerWebExchange): String =
		exchange.getAttribute<String>(CorrelationIdFilter.ATTRIBUTE_NAME)
			?: exchange.request.headers.getFirst(CorrelationIdFilter.HEADER_NAME)
			?: "unknown"

	private fun resolveCode(statusCode: Int, statusName: String?): String = when {
		!statusName.isNullOrBlank() -> statusName
		else -> statusCode.toString()
	}

	private fun FieldError.asMessage(): String = "$field: ${defaultMessage ?: "invalid value"}"
}
