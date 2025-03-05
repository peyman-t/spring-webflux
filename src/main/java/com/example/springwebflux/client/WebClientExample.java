package com.example.springwebflux.client;

import com.example.springwebflux.model.Product;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class demonstrates how to use WebClient to interact with our WebFlux API.
 * It's not used in the application but serves as an example of reactive client-side code.
 */
public class WebClientExample {

    private final WebClient webClient;

    public WebClientExample(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Flux<Product> getAllProducts() {
        return webClient.get()
                .uri("/api/products")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Product.class);
    }

    public Mono<Product> getProductById(String id) {
        return webClient.get()
                .uri("/api/products/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Product.class);
    }

    public Flux<Product> getProductsCheaperThan(double maxPrice) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products")
                        .queryParam("cheaperThan", maxPrice)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Product.class);
    }

    public Mono<Product> createProduct(Product product) {
        return webClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Product.class);
    }

    public Mono<Product> updateProduct(String id, Product product) {
        return webClient.put()
                .uri("/api/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Product.class);
    }

    public Mono<Void> deleteProduct(String id) {
        return webClient.delete()
                .uri("/api/products/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /**
     * Example of streaming events using WebClient
     */
    public Flux<Product> streamProductUpdates() {
        return webClient.get()
                .uri("/api/products/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Product.class);
    }

    /**
     * Example of error handling with WebClient
     */
    public Mono<Product> getProductByIdWithErrorHandling(String id) {
        return webClient.get()
                .uri("/api/products/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("Product not found")))
                .bodyToMono(Product.class)
                .onErrorResume(error -> {
                    System.err.println("Error fetching product: " + error.getMessage());
                    return Mono.empty();
                });
    }

    // Example of how to use this client
    public static void main(String[] args) {
        WebClientExample client = new WebClientExample("http://localhost:8080");

        // Get all products
        client.getAllProducts()
                .doOnNext(product -> System.out.println("Product: " + product))
                .blockLast(); // Block for demo purposes only

        // Create a new product
        Product newProduct = new Product(null, "WebClient Product", 299.99);
        client.createProduct(newProduct)
                .doOnNext(product -> System.out.println("Created: " + product))
                .block(); // Block for demo purposes only

        // Stream updates (would normally subscribe without blocking)
        client.streamProductUpdates()
                .doOnNext(product -> System.out.println("Update: " + product))
                .subscribe();

        // Keep application running for streaming demo
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
