package com.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RecommendationApplicationTests {

	@Autowired
	private WebTestClient testClient;

	@Test
	void testClientNotNull() {
		assertNotNull(testClient);
	}

	@Test
	void getRecommendationsByProductId() {
		int productId = 1;
		testClient.get()
			.uri("/recommendations?productId=" + productId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[0].productId").isEqualTo(productId);
	}

	@Test
	void getRecommendationsMissingParameters() {
		testClient.get()
			.uri("/recommendations")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/recommendations")
				.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void getRecommendationsInvalidParameter() {

		testClient.get()
			.uri("/recommendations?productId=no-integer")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/recommendations")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsNotFound() {

		int productIdNotFound = 113;

		testClient.get()
			.uri("/recommendations?productId=" + productIdNotFound)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		testClient.get()
			.uri("/recommendations?productId=" + productIdInvalid)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/recommendations")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}
}
