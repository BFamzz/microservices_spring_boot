package com.microservices.composite.product.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RestController;

import com.microservices.api.composite.product.ProductAggregate;
import com.microservices.api.composite.product.ProductCompositeService;
import com.microservices.api.composite.product.RecommendationSummary;
import com.microservices.api.composite.product.ReviewSummary;
import com.microservices.api.composite.product.ServiceAddresses;
import com.microservices.api.core.product.Product;
import com.microservices.api.core.recommendation.Recommendation;
import com.microservices.api.core.review.Review;
import com.microservices.api.exceptions.NotFoundException;
import com.microservices.util.http.ServiceUtil;

@RestController
public class CompositeImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private CompositeIntegration integration;

    public CompositeImpl(ServiceUtil serviceUtil, CompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);
        List<Review> reviews = integration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product,
        List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        
        int productId  = product.getProductId();
        String productName = product.getName();
        int productWeight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
            recommendations.stream().map((Recommendation recommendation) -> new RecommendationSummary(
                recommendation.getRecommendationId(), recommendation.getAuthor(), recommendation.getRating()))
                .collect(Collectors.toList());
        
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream().map((Review review) -> new ReviewSummary(review.getReviewId(), review.getAuthor(), review.getSubject()))
                .collect(Collectors.toList());

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";

        ServiceAddresses  serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, productName, productWeight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }    
}
