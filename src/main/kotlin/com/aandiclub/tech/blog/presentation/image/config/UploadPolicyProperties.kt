package com.aandiclub.tech.blog.presentation.image.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.upload")
data class UploadPolicyProperties(
	val maxSizeBytes: Long = 5 * 1024 * 1024,
	val allowedContentTypes: List<String> = listOf(
		"image/png",
		"image/jpeg",
		"image/gif",
		"image/webp",
	),
)
