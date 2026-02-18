package com.aandiclub.tech.blog.presentation.post.service

import com.aandiclub.tech.blog.domain.post.Post
import com.aandiclub.tech.blog.domain.post.PostStatus
import com.aandiclub.tech.blog.infrastructure.post.PostRepository
import com.aandiclub.tech.blog.presentation.post.dto.CreatePostRequest
import com.aandiclub.tech.blog.presentation.post.dto.PagedPostResponse
import com.aandiclub.tech.blog.presentation.post.dto.PatchPostRequest
import com.aandiclub.tech.blog.presentation.post.dto.PostResponse
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil

@Service
class PostServiceImpl(
	private val postRepository: PostRepository,
) : PostService {

	override suspend fun create(request: CreatePostRequest): PostResponse {
		val post = Post(
			title = request.title,
			contentMarkdown = request.contentMarkdown,
			authorId = request.authorId,
			status = request.status,
		)
		return postRepository.save(post).toResponse()
	}

	override suspend fun get(postId: UUID): PostResponse =
		postRepository.findByIdAndStatusNot(postId, PostStatus.Deleted.name)?.toResponse()
			?: throw notFound(postId)

	override suspend fun list(page: Int, size: Int, status: PostStatus?): PagedPostResponse {
		if (status == PostStatus.Draft) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "draft posts are only available in draft list")
		}
		return listByStatus(page, size, (status ?: PostStatus.Published).name)
	}

	override suspend fun listDrafts(page: Int, size: Int): PagedPostResponse =
		listByStatus(page, size, PostStatus.Draft.name)

	private suspend fun listByStatus(page: Int, size: Int, status: String): PagedPostResponse {
		val items = postRepository.findPageByStatus(status, size, (page * size).toLong())
			.map { it.toResponse() }
			.toList()
		val totalElements = postRepository.countByStatus(status)
		val totalPages = if (totalElements == 0L) 0 else ceil(totalElements.toDouble() / size.toDouble()).toInt()

		return PagedPostResponse(
			items = items,
			page = page,
			size = size,
			totalElements = totalElements,
			totalPages = totalPages,
		)
	}

	override suspend fun patch(postId: UUID, request: PatchPostRequest): PostResponse {
		val current = postRepository.findByIdAndStatusNot(postId, PostStatus.Deleted.name) ?: throw notFound(postId)
		val updated = current.copy(
			title = request.title ?: current.title,
			contentMarkdown = request.contentMarkdown ?: current.contentMarkdown,
			status = request.status ?: current.status,
			updatedAt = Instant.now(),
		)
		return postRepository.save(updated).toResponse()
	}

	override suspend fun delete(postId: UUID) {
		val current = postRepository.findByIdAndStatusNot(postId, PostStatus.Deleted.name) ?: throw notFound(postId)
		postRepository.save(
			current.copy(
				status = PostStatus.Deleted,
				updatedAt = Instant.now(),
			),
		)
	}

	private fun notFound(postId: UUID): ResponseStatusException =
		ResponseStatusException(HttpStatus.NOT_FOUND, "post not found: $postId")

	private fun Post.toResponse() = PostResponse(
		id = id,
		title = title,
		contentMarkdown = contentMarkdown,
		authorId = authorId,
		status = status,
		createdAt = createdAt,
		updatedAt = updatedAt,
	)
}
