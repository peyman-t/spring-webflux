package com.example.springwebflux.controller;

import com.example.springwebflux.model.Product;
import com.example.springwebflux.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @Test
    public void testGetAllProducts() {
        Product product1 = new Product("1", "Test Product 1", 99.99);
        Product product2 = new Product("2", "Test Product 2", 199.99);

        when(productService.getAllProducts())
                .thenReturn(Flux.just(product1, product2));

        webTestClient.get()
                .uri("/api/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(2)
                .contains(product1, product2);
    }

    @Test
    public void testGetProductById() {
        Product product = new Product("1", "Test Product", 99.99);

        when(productService.getProductById("1"))
                .thenReturn(Mono.just(product));

        webTestClient.get()
                .uri("/api/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(product);
    }

    @Test
    public void testProductNotFound() {
        when(productService.getProductById("999"))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testCreateProduct() {
        Product product = new Product(null, "New Product", 99.99);
        Product savedProduct = new Product("1", "New Product", 99.99);

        when(productService.createProduct(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .isEqualTo(savedProduct);
    }
}
