package com.aandiclub.tech.blog.presentation.post.dto

import com.aandiclub.tech.blog.domain.post.PostStatus
import jakarta.validation.constraints.Size

data class PatchPostRequest(
	@field:Size(min = 1, max = 200)
	val title: String? = null,
	val contentMarkdown: String? = null,
	val status: PostStatus? = null,
)
