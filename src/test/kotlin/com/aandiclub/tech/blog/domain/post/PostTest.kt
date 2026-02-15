package com.aandiclub.tech.blog.domain.post

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class PostTest : StringSpec({
	"title must be between 1 and 200 characters" {
		shouldThrow<IllegalArgumentException> {
			Post(
				title = "",
				contentMarkdown = "content",
				authorId = UUID.randomUUID(),
			)
		}

		shouldThrow<IllegalArgumentException> {
			Post(
				title = "a".repeat(201),
				contentMarkdown = "content",
				authorId = UUID.randomUUID(),
			)
		}
	}

	"content markdown must not be blank" {
		shouldThrow<IllegalArgumentException> {
			Post(
				title = "valid title",
				contentMarkdown = " ",
				authorId = UUID.randomUUID(),
			)
		}
	}

	"default status should be Draft" {
		val post = Post(
			title = "valid title",
			contentMarkdown = "content",
			authorId = UUID.randomUUID(),
		)

		post.status shouldBe PostStatus.Draft
	}
})
