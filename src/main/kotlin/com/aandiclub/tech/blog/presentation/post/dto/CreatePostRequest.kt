package com.aandiclub.tech.blog.presentation.post.dto

import com.aandiclub.tech.blog.domain.post.PostStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreatePostRequest(
	@field:NotBlank
	@field:Size(max = 200)
	val title: String,
	@field:NotBlank
	val contentMarkdown: String,
	@field:NotNull
	val authorId: UUID,
	val status: PostStatus = PostStatus.Published,
)
