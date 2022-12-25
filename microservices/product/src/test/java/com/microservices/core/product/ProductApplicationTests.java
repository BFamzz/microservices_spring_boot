package com.microservices.core.product;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductApplicationTests {

	@Autowired
	private WebTestClient testClient;

	@Test
	void testClientNotNull() {
		assertNotNull(testClient);
	}

	@Test
	void getProductById() {
		int productId = 1;
		testClient.get()
			.uri("/products/" + productId)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.productId").isEqualTo(productId);
	}

	@Test
	void getProductInvalidParameterString() {
		testClient.get()
			.uri("/products/no-integer")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/products/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getProductNotFound() {
		int productIdNotFound = 13;
		testClient.get()
			.uri("/products/" + productIdNotFound)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/products/" + productIdNotFound).
				jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}

	@Test
	void getProductInvalidParameterNegativeValue() {
		int productIdInvalid = -1;
		testClient.get()
			.uri("/products/" + productIdInvalid)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/products/" + productIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}
}
