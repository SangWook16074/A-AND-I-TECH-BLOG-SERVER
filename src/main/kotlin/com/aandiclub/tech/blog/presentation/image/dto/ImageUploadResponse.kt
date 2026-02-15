package com.aandiclub.tech.blog.presentation.image.dto

data class ImageUploadResponse(
	val url: String,
	val key: String,
	val contentType: String,
	val size: Long,
)
