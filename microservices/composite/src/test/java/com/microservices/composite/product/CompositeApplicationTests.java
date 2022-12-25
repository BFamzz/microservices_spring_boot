package com.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.microservices.api.core.product.Product;
import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.review.Review;
import com.microservices.api.exceptions.InvalidInputException;
import com.microservices.api.exceptions.NotFoundException;
import com.microservices.composite.product.services.CompositeIntegration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CompositeApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired
	private WebTestClient testClient;

	@MockBean
	private CompositeIntegration compositeIntegration;

	@Test
	void testClientNotNull() {
		assertNotNull(testClient);
	}

	@Test
	void compositeIntegrationNotNull() {
		assertNotNull(compositeIntegration);
	}

	@BeforeEach
	void setup() {
		when(compositeIntegration.getProduct(PRODUCT_ID_OK))
			.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
		
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
			.thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address")));
		
		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
			.thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, null, "author", "content", "mock-address")));

		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
			.thenThrow(new NotFoundException("NOT FOUND! ProductId is: " + PRODUCT_ID_NOT_FOUND));

		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
			.thenThrow(new InvalidInputException("INVALID! ProductId is: " + PRODUCT_ID_INVALID));
	}

	@Test
	void getProductById() {
		testClient.get()
			.uri("/products-composite/" + PRODUCT_ID_OK)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	void getProductNotFound() {
		testClient.get()
			.uri("/products-composite/" + PRODUCT_ID_NOT_FOUND)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/products-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND! ProductId is: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidInput() {

		testClient.get()
			.uri("/products-composite/" + PRODUCT_ID_INVALID)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
				.jsonPath("$.path").isEqualTo("/products-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID! ProductId is: " + PRODUCT_ID_INVALID);
	}
}
