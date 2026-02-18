package com.aandiclub.tech.blog.presentation.post

import com.aandiclub.tech.blog.domain.post.PostStatus
import com.aandiclub.tech.blog.presentation.post.dto.CreatePostRequest
import com.aandiclub.tech.blog.presentation.post.dto.PagedPostResponse
import com.aandiclub.tech.blog.presentation.post.dto.PatchPostRequest
import com.aandiclub.tech.blog.presentation.post.dto.PostResponse
import com.aandiclub.tech.blog.presentation.post.service.PostService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/v1/posts")
@Tag(name = "Posts", description = "Post CRUD API")
class PostController(
	private val postService: PostService,
) {
	@PostMapping
	@Operation(summary = "Create post")
	@ApiResponses(
		value = [
			ApiResponse(responseCode = "201", description = "Created", content = [Content(schema = Schema(implementation = PostResponse::class))]),
			ApiResponse(responseCode = "400", description = "Validation failed"),
		],
	)
	suspend fun create(@Valid @RequestBody request: CreatePostRequest): ResponseEntity<PostResponse> =
		ResponseEntity.status(201).body(postService.create(request))

	@GetMapping("/{postId}")
	@Operation(summary = "Get post detail")
	@ApiResponses(
		value = [
			ApiResponse(responseCode = "200", description = "OK", content = [Content(schema = Schema(implementation = PostResponse::class))]),
			ApiResponse(responseCode = "404", description = "Not found"),
		],
	)
	suspend fun get(@PathVariable postId: UUID): ResponseEntity<PostResponse> =
		ResponseEntity.ok(postService.get(postId))

	@GetMapping
	@Operation(summary = "List posts")
	@ApiResponses(
		value = [
			ApiResponse(responseCode = "200", description = "OK", content = [Content(schema = Schema(implementation = PagedPostResponse::class))]),
		],
	)
	suspend fun list(
		@RequestParam(defaultValue = "0") @Min(0) page: Int,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
		@RequestParam(required = false) status: PostStatus?,
	): ResponseEntity<PagedPostResponse> =
		ResponseEntity.ok(postService.list(page, size, status))

	@PatchMapping("/{postId}")
	@Operation(summary = "Patch post")
	@ApiResponses(
		value = [
			ApiResponse(responseCode = "200", description = "OK", content = [Content(schema = Schema(implementation = PostResponse::class))]),
			ApiResponse(responseCode = "404", description = "Not found"),
		],
	)
	suspend fun patch(
		@PathVariable postId: UUID,
		@Valid @RequestBody request: PatchPostRequest,
	): ResponseEntity<PostResponse> =
		ResponseEntity.ok(postService.patch(postId, request))

	@DeleteMapping("/{postId}")
	@Operation(summary = "Delete post")
	@ApiResponses(
		value = [
			ApiResponse(responseCode = "204", description = "No content"),
			ApiResponse(responseCode = "404", description = "Not found"),
		],
	)
	suspend fun delete(@PathVariable postId: UUID): ResponseEntity<Void> {
		postService.delete(postId)
		return ResponseEntity.noContent().build()
	}
}
