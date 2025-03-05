package com.example.springwebflux.service;

import com.example.springwebflux.model.Product;
import com.example.springwebflux.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    public void testGetAllProducts() {
        Product product1 = new Product("1", "Test Product 1", 99.99);
        Product product2 = new Product("2", "Test Product 2", 199.99);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(Flux.fromIterable(products));

        Flux<Product> result = productService.getAllProducts();

        StepVerifier.create(result)
                .expectNext(product1, product2)
                .verifyComplete();
    }

    @Test
    public void testGetAllProductsSorted() {
        Product product1 = new Product("1", "B Test Product", 99.99);
        Product product2 = new Product("2", "A Test Product", 199.99);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(Flux.fromIterable(products));

        Flux<Product> result = productService.getAllProductsSorted();

        StepVerifier.create(result)
                .expectNext(product2, product1) // A should come before B
                .verifyComplete();
    }

    @Test
    public void testGetProductsCheaperThan() {
        Product product1 = new Product("1", "Cheap Product", 49.99);
        Product product2 = new Product("2", "Expensive Product", 149.99);
        List<Product> products = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(Flux.fromIterable(products));

        Flux<Product> result = productService.getProductsCheaperThan(100.0);

        StepVerifier.create(result)
                .expectNext(product1) // Only the cheap product should be returned
                .verifyComplete();
    }

    @Test
    public void testGetProductById() {
        Product product = new Product("1", "Test Product", 99.99);

        when(productRepository.findById("1")).thenReturn(Mono.just(product));

        Mono<Product> result = productService.getProductById("1");

        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    public void testCreateProduct() {
        Product product = new Product(null, "New Product", 99.99);
        Product savedProduct = new Product("generated-id", "New Product", 99.99);

        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(savedProduct));

        Mono<Product> result = productService.createProduct(product);

        StepVerifier.create(result)
                .expectNext(savedProduct)
                .verifyComplete();
    }
}
