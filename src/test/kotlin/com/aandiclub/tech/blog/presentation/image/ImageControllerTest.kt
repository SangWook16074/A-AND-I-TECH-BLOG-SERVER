package com.aandiclub.tech.blog.presentation.image

import com.aandiclub.tech.blog.presentation.image.dto.ImageUploadResponse
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.mockk
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.test.web.reactive.server.WebTestClient

class ImageControllerTest : StringSpec({
	val service = mockk<ImageUploadService>()
	val webTestClient = WebTestClient.bindToController(ImageController(service)).build()

	"POST /v1/images should return upload metadata" {
		coEvery { service.upload(any()) } returns ImageUploadResponse(
			url = "https://bucket.s3.us-east-1.amazonaws.com/images/abc.png",
			key = "images/abc.png",
			contentType = "image/png",
			size = 4,
		)

		val bodyBuilder = MultipartBodyBuilder()
		bodyBuilder.part(
			"file",
			object : ByteArrayResource(byteArrayOf(1, 2, 3, 4)) {
				override fun getFilename(): String = "abc.png"
			},
		).contentType(MediaType.IMAGE_PNG)

		webTestClient.post()
			.uri("/v1/images")
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(bodyBuilder.build()))
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.url").isEqualTo("https://bucket.s3.us-east-1.amazonaws.com/images/abc.png")
			.jsonPath("$.key").isEqualTo("images/abc.png")
			.jsonPath("$.contentType").isEqualTo("image/png")
			.jsonPath("$.size").isEqualTo(4)
	}
})
