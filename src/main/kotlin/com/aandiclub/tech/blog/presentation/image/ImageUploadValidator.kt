package com.aandiclub.tech.blog.presentation.image

import com.aandiclub.tech.blog.presentation.image.config.UploadPolicyProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ImageUploadValidator(
	private val uploadPolicyProperties: UploadPolicyProperties,
) {
	fun validate(contentType: String, size: Long) {
		if (!uploadPolicyProperties.allowedContentTypes.contains(contentType)) {
			throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported content type: $contentType")
		}
		if (size > uploadPolicyProperties.maxSizeBytes) {
			throw ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "file size exceeds limit")
		}
	}
}
