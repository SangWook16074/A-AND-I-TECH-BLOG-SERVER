package com.aandiclub.tech.blog.presentation.post.dto

data class PagedPostResponse(
	val items: List<PostResponse>,
	val page: Int,
	val size: Int,
	val totalElements: Long,
	val totalPages: Int,
)
