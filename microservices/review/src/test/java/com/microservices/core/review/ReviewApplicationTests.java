package com.microservices.core.review;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReviewApplicationTests {

	@Autowired
	private WebTestClient testClient;

	@Test
	void testClientNotNull() {
		assertNotNull(testClient);
	}

	@Test
	void getReviewsByProductId() {
		int productId = 1;
		testClient.get()
			.uri("/reviews?productId=" + productId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[0].productId").isEqualTo(productId);
	}

	@Test
	void getReviewsMissingParameters() {
		testClient.get()
			.uri("/reviews")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void getReviewsInvalidParameter() {

		testClient.get()
			.uri("/reviews?productId=no-integer")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getReviewsNotFound() {

		int productIdNotFound = 213;

		testClient.get()
			.uri("/reviews?productId=" + productIdNotFound)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getReviewsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		testClient.get()
			.uri("/reviews?productId=" + productIdInvalid)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}
}
