package com.aandiclub.tech.blog.common

import com.aandiclub.tech.blog.common.error.GlobalExceptionHandler
import com.aandiclub.tech.blog.common.filter.CorrelationIdFilter
import com.aandiclub.tech.blog.domain.post.PostStatus
import com.aandiclub.tech.blog.presentation.post.PostController
import com.aandiclub.tech.blog.presentation.post.dto.CreatePostRequest
import com.aandiclub.tech.blog.presentation.post.service.PostService
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class ErrorHandlingTest : StringSpec({
	val postService = mockk<PostService>()
	val client = WebTestClient.bindToController(PostController(postService))
		.controllerAdvice(GlobalExceptionHandler())
		.build()

	"response status exception should follow error response format with request traceId" {
		coEvery { postService.get(any()) } throws ResponseStatusException(HttpStatus.NOT_FOUND, "post not found")
		val postId = UUID.randomUUID()

		client.get()
			.uri("/v1/posts/$postId")
			.header(CorrelationIdFilter.HEADER_NAME, "trace-123")
			.exchange()
			.expectStatus().isNotFound
			.expectBody()
			.jsonPath("$.code").isEqualTo("NOT_FOUND")
			.jsonPath("$.message").isEqualTo("post not found")
			.jsonPath("$.timestamp").exists()
			.jsonPath("$.path").isEqualTo("/v1/posts/$postId")
			.jsonPath("$.traceId").isEqualTo("trace-123")
	}

	"validation error should return standardized body" {
		client.post()
			.uri("/v1/posts")
			.header(CorrelationIdFilter.HEADER_NAME, "trace-validation-1")
			.bodyValue(
				CreatePostRequest(
					title = "",
					contentMarkdown = "content",
					authorId = UUID.randomUUID(),
					status = PostStatus.Draft,
				),
			)
				.exchange()
				.expectStatus().isBadRequest
				.expectBody()
				.jsonPath("$.code").isEqualTo("VALIDATION_FAILED")
				.jsonPath("$.timestamp").exists()
				.jsonPath("$.path").isEqualTo("/v1/posts")
				.jsonPath("$.traceId").isEqualTo("trace-validation-1")
	}
})
