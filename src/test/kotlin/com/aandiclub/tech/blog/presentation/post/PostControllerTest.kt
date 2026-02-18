package com.aandiclub.tech.blog.presentation.post

import com.aandiclub.tech.blog.domain.post.PostStatus
import com.aandiclub.tech.blog.presentation.post.dto.CreatePostRequest
import com.aandiclub.tech.blog.presentation.post.dto.PagedPostResponse
import com.aandiclub.tech.blog.presentation.post.dto.PatchPostRequest
import com.aandiclub.tech.blog.presentation.post.dto.PostResponse
import com.aandiclub.tech.blog.presentation.post.service.PostService
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

class PostControllerTest : StringSpec({
	val service = mockk<PostService>()
	val webTestClient = WebTestClient.bindToController(PostController(service)).build()

	"POST /v1/posts should return 201" {
		val id = UUID.randomUUID()
		val authorId = UUID.randomUUID()
		val now = Instant.parse("2026-02-15T12:00:00Z")
		val response = PostResponse(
			id = id,
			title = "title",
			contentMarkdown = "content",
			authorId = authorId,
			status = PostStatus.Draft,
			createdAt = now,
			updatedAt = now,
		)

		coEvery { service.create(any()) } returns response

		webTestClient.post()
			.uri("/v1/posts")
			.bodyValue(
				CreatePostRequest(
					title = "title",
					contentMarkdown = "content",
					authorId = authorId,
					status = PostStatus.Draft,
				),
			)
			.exchange()
			.expectStatus().isCreated
			.expectBody()
			.jsonPath("$.id").isEqualTo(id.toString())
			.jsonPath("$.status").isEqualTo("Draft")
	}

	"GET /v1/posts/{id} should return 404 when not found" {
		coEvery { service.get(any()) } throws ResponseStatusException(HttpStatus.NOT_FOUND)

		webTestClient.get()
			.uri("/v1/posts/${UUID.randomUUID()}")
			.exchange()
			.expectStatus().isNotFound
	}

	"GET /v1/posts should return paged response" {
		val authorId = UUID.randomUUID()
		val now = Instant.parse("2026-02-15T12:00:00Z")
		coEvery { service.list(0, 20, null) } returns
			PagedPostResponse(
				items = listOf(
					PostResponse(
						id = UUID.randomUUID(),
						title = "title",
						contentMarkdown = "content",
						authorId = authorId,
						status = PostStatus.Published,
						createdAt = now,
						updatedAt = now,
					),
				),
				page = 0,
				size = 20,
				totalElements = 1,
				totalPages = 1,
			)

		webTestClient.get()
			.uri("/v1/posts?page=0&size=20")
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.page").isEqualTo(0)
			.jsonPath("$.size").isEqualTo(20)
			.jsonPath("$.totalElements").isEqualTo(1)
			.jsonPath("$.totalPages").isEqualTo(1)
			.jsonPath("$.items[0].status").isEqualTo("Published")
	}

	"PATCH /v1/posts/{id} should return 200" {
		val id = UUID.randomUUID()
		val authorId = UUID.randomUUID()
		val now = Instant.parse("2026-02-15T12:00:00Z")
		coEvery { service.patch(eq(id), any()) } returns
			PostResponse(
				id = id,
				title = "updated",
				contentMarkdown = "updated-content",
				authorId = authorId,
				status = PostStatus.Published,
				createdAt = now,
				updatedAt = now,
			)

		webTestClient.patch()
			.uri("/v1/posts/$id")
			.bodyValue(
				PatchPostRequest(
					title = "updated",
					contentMarkdown = "updated-content",
					status = PostStatus.Published,
				),
			)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.title").isEqualTo("updated")
			.jsonPath("$.status").isEqualTo("Published")
	}

	"DELETE /v1/posts/{id} should return 204" {
		coEvery { service.delete(any()) } returns Unit

		webTestClient.delete()
			.uri("/v1/posts/${UUID.randomUUID()}")
			.exchange()
			.expectStatus().isNoContent
	}
})
