package com.microservices.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.api.core.product.Product;
import com.microservices.api.core.product.ProductService;
import com.microservices.api.exceptions.InvalidInputException;
import com.microservices.api.exceptions.NotFoundException;
import com.microservices.util.http.ServiceUtil;

@RestController
public class ProductImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductImpl.class);
    private final ServiceUtil serviceUtil;

    public ProductImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        LOG.debug("/product returned the found product for productId = {}", productId);
        
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 13) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
}
