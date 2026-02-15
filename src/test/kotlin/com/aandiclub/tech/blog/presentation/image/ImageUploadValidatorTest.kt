package com.aandiclub.tech.blog.presentation.image

import com.aandiclub.tech.blog.presentation.image.config.UploadPolicyProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ImageUploadValidatorTest : StringSpec({
	val validator = ImageUploadValidator(
		UploadPolicyProperties(
			maxSizeBytes = 1024,
			allowedContentTypes = listOf("image/png", "image/jpeg"),
		),
	)

	"unsupported content type should throw 415" {
		val exception = shouldThrow<ResponseStatusException> {
			validator.validate("application/pdf", 100)
		}
		exception.statusCode shouldBe HttpStatus.UNSUPPORTED_MEDIA_TYPE
	}

	"payload too large should throw 413" {
		val exception = shouldThrow<ResponseStatusException> {
			validator.validate("image/png", 1025)
		}
		exception.statusCode shouldBe HttpStatus.PAYLOAD_TOO_LARGE
	}
})
