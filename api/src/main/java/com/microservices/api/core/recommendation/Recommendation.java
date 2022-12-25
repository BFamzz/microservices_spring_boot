package com.microservices.api.core.recommendation;

public class Recommendation {
    private final int productId;
    private final int recommendationId;
    private final String author;
    private final int rating;
    private final String content;
    private final String serviceAddress;

    public Recommendation() {
        this.productId = 0;
        this.recommendationId = 0;
        this.author = null;
        this.rating = 0;
        this.content = null;
        this.serviceAddress = null;
    }

    public Recommendation(int productId, int recommendationId, String author, int rating, String content, String serviceAddress) {
        this.productId = productId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.rating = rating;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public int getProductId() {
        return productId;
    }

    public int getRecommendationId() {
        return recommendationId;
    }

    public String getAuthor() {
        return author;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}
