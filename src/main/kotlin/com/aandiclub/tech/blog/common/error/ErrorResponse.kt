package com.aandiclub.tech.blog.common.error

import java.time.Instant

data class ErrorResponse(
	val code: String,
	val message: String,
	val timestamp: Instant,
	val path: String,
	val traceId: String,
)
