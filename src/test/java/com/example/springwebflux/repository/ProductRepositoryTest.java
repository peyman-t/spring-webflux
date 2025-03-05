package com.example.springwebflux.repository;

import com.example.springwebflux.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ProductRepositoryTest {

    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        productRepository = new ProductRepository();
    }

    @Test
    public void testFindAll() {
        Flux<Product> productFlux = productRepository.findAll();

        StepVerifier.create(productFlux)
                .expectNextCount(5) // There are 5 sample products
                .verifyComplete();
    }

    @Test
    public void testFindById() {
        Mono<Product> productMono = productRepository.findById("1");

        StepVerifier.create(productMono)
                .expectNextMatches(p -> p.getId().equals("1") && p.getName().equals("Laptop"))
                .verifyComplete();
    }

    @Test
    public void testSave() {
        Product newProduct = new Product("6", "New Product", 29.99);
        Mono<Product> savedProductMono = productRepository.save(newProduct);

        StepVerifier.create(savedProductMono)
                .expectNext(newProduct)
                .verifyComplete();

        // Verify it was actually saved
        Mono<Product> retrievedProductMono = productRepository.findById("6");

        StepVerifier.create(retrievedProductMono)
                .expectNext(newProduct)
                .verifyComplete();
    }

    @Test
    public void testDeleteById() {
        // First verify the product exists
        Mono<Product> productMono = productRepository.findById("1");

        StepVerifier.create(productMono)
                .expectNextCount(1)
                .verifyComplete();

        // Delete the product
        Mono<Void> deleteMono = productRepository.deleteById("1");

        StepVerifier.create(deleteMono)
                .verifyComplete();

        // Verify it was deleted
        Mono<Product> deletedProductMono = productRepository.findById("1");

        StepVerifier.create(deletedProductMono)
                .verifyComplete(); // No elements expected
    }
}
