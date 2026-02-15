package com.aandiclub.tech.blog.presentation.image

import com.aandiclub.tech.blog.presentation.image.dto.ImageUploadResponse
import org.springframework.http.codec.multipart.FilePart

interface ImageUploadService {
	suspend fun upload(filePart: FilePart): ImageUploadResponse
}
