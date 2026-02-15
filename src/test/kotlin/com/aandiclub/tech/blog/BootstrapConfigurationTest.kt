package com.aandiclub.tech.blog

import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.DockerClientFactory

@SpringBootTest
@ActiveProfiles("test")
class BootstrapConfigurationTest : StringSpec() {

	@Autowired
	lateinit var applicationContext: ApplicationContext

	@Autowired
	lateinit var environment: Environment

	override fun extensions() = listOf(SpringExtension)

	init {
		"test profile should define r2dbc testcontainers url" {
			assumeTrue(runCatching { DockerClientFactory.instance().isDockerAvailable }.getOrDefault(false))
			val url = environment.getProperty("spring.r2dbc.url")
			url.shouldNotBeNull()
			url.startsWith("r2dbc:tc:postgresql").shouldBeTrue()
		}

		"context should expose reactive database beans" {
			assumeTrue(runCatching { DockerClientFactory.instance().isDockerAvailable }.getOrDefault(false))
			applicationContext.containsBean("connectionFactory").shouldBeTrue()
			applicationContext.containsBean("databaseClient").shouldBeTrue()
		}
	}
}
