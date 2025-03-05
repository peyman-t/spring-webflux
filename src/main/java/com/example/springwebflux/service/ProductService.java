package com.example.springwebflux.service;

import com.example.springwebflux.model.Product;
import com.example.springwebflux.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Comparator;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    // For broadcasting product events to clients (for SSE)
    private final Sinks.Many<Product> productSink = Sinks.many().multicast().onBackpressureBuffer();

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Flux<Product> getAllProductsSorted() {
        return productRepository.findAll()
                .sort(Comparator.comparing(Product::getName));
    }

    public Flux<Product> getProductsCheaperThan(double maxPrice) {
        return productRepository.findAll()
                .filter(product -> product.getPrice() < maxPrice);
    }

    public Mono<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Mono<Product> createProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        return productRepository.save(product)
                .doOnNext(p -> productSink.tryEmitNext(p));
    }

    public Mono<Product> updateProduct(String id, Product product) {
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                    product.setId(id);
                    return productRepository.save(product);
                })
                .doOnNext(p -> productSink.tryEmitNext(p));
    }

    public Mono<Void> deleteProduct(String id) {
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                    productSink.tryEmitNext(existingProduct);  // Notify about deletion
                    return productRepository.deleteById(id);
                });
    }

    public Flux<Product> getProductUpdates() {
        return productSink.asFlux();
    }
}
