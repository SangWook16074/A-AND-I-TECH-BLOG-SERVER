package com.aandiclub.tech.blog.infrastructure.post

import com.aandiclub.tech.blog.domain.post.Post
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface PostRepository : CoroutineCrudRepository<Post, UUID> {
	@Query(
		"""
		SELECT * FROM posts
		WHERE id = :id
		  AND status::text <> :status
		LIMIT 1
		""",
	)
	suspend fun findByIdAndStatusNot(id: UUID, status: String): Post?

	@Query(
		"""
		SELECT * FROM posts
		WHERE status::text = :status
		ORDER BY created_at DESC
		LIMIT :limit OFFSET :offset
		""",
	)
	fun findPageByStatus(status: String, limit: Int, offset: Long): Flow<Post>

	@Query(
		"""
		SELECT COUNT(*) FROM posts
		WHERE status::text = :status
		""",
	)
	suspend fun countByStatus(status: String): Long
}
