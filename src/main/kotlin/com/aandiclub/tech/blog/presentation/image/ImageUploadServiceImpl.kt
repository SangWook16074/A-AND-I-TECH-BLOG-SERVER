package com.aandiclub.tech.blog.presentation.image

import com.aandiclub.tech.blog.infrastructure.s3.S3Properties
import com.aandiclub.tech.blog.presentation.image.dto.ImageUploadResponse
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Service
class ImageUploadServiceImpl(
	private val imageUploadValidator: ImageUploadValidator,
	private val s3AsyncClient: S3AsyncClient,
	private val s3Properties: S3Properties,
) : ImageUploadService {
	override suspend fun upload(filePart: FilePart): ImageUploadResponse {
		val contentType = filePart.headers().contentType?.toString()
			?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "missing content type")
		val bytes = filePart.readBytes()
		imageUploadValidator.validate(contentType, bytes.size.toLong())

		val key = "images/${UUID.randomUUID()}.${contentType.substringAfter('/', "bin")}"
		val request = PutObjectRequest.builder()
			.bucket(s3Properties.bucket)
			.key(key)
			.contentType(contentType)
			.contentLength(bytes.size.toLong())
			.build()
		s3AsyncClient.putObject(request, AsyncRequestBody.fromBytes(bytes)).await()

		return ImageUploadResponse(
			url = resolvePublicUrl(key),
			key = key,
			contentType = contentType,
			size = bytes.size.toLong(),
		)
	}

	private suspend fun FilePart.readBytes(): ByteArray {
		val joined = DataBufferUtils.join(content()).awaitSingle()
		try {
			val bytes = ByteArray(joined.readableByteCount())
			joined.read(bytes)
			return bytes
		} finally {
			DataBufferUtils.release(joined)
		}
	}

	private fun resolvePublicUrl(key: String): String {
		if (s3Properties.publicBaseUrl.isNotBlank()) {
			return "${s3Properties.publicBaseUrl.trimEnd('/')}/$key"
		}
		return "https://${s3Properties.bucket}.s3.${s3Properties.region}.amazonaws.com/$key"
	}
}
