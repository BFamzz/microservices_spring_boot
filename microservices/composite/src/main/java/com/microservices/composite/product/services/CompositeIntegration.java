package com.microservices.composite.product.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.api.core.product.Product;
import com.microservices.api.core.product.ProductService;
import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.recommendation.RecommendationService;
import com.microservices.api.core.review.Review;
import com.microservices.api.core.review.ReviewService;
import com.microservices.api.exceptions.InvalidInputException;
import com.microservices.api.exceptions.NotFoundException;
import com.microservices.util.http.HttpErrorInfo;

@Component
public class CompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    public CompositeIntegration(RestTemplate restTemplate, ObjectMapper objectMapper,
        @Value("${app.product.host}") String productServiceHost,
        @Value("${app.product.port}") String productServicePort,
        @Value("${app.recommendation.host}") String recommendationServiceHost,
        @Value("${app.recommendation.port}") String recommendationServicePort,
        @Value("${app.review.host}") String reviewServiceHost,
        @Value("${app.review.port}") String reviewServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/products/";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendations?productId=";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/reviews?productId=";
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Calling getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with Id: {}", product.getProductId());

            return product;
        } catch (HttpClientErrorException e) {
            switch ((HttpStatus) e.getStatusCode()) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(e));

                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(e));

                default:
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", e.getStatusCode());
                    LOG.warn("Error body: {}", e.getResponseBodyAsString());
                    throw e;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;
            LOG.debug("Calling /recommendations API on url: {}", url);

            List<Recommendation> recommendations = restTemplate
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                .getBody();

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (Exception e) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;
            LOG.debug("Calling /reviews API on url: {}", url);

            List<Review> reviews = restTemplate
                .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {})
                .getBody();
                
            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (Exception e) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
    }    
}
