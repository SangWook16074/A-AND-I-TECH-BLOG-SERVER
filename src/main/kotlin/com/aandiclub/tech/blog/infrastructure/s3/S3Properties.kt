package com.aandiclub.tech.blog.infrastructure.s3

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.s3")
data class S3Properties(
	val bucket: String = "",
	val region: String = "us-east-1",
	val publicBaseUrl: String = "",
)
