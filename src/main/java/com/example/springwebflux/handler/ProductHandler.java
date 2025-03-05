package com.example.springwebflux.handler;

import com.example.springwebflux.model.Product;
import com.example.springwebflux.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class ProductHandler {

    private final ProductService productService;

    @Autowired
    public ProductHandler(ProductService productService) {
        this.productService = productService;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.getAllProducts(), Product.class);
    }

    public Mono<ServerResponse> getSortedProducts(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.getAllProductsSorted(), Product.class);
    }

    public Mono<ServerResponse> getCheaperProducts(ServerRequest request) {
        return request.queryParam("cheaperThan")
                .map(priceStr -> {
                    try {
                        double price = Double.parseDouble(priceStr);
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productService.getProductsCheaperThan(price), Product.class);
                    } catch (NumberFormatException e) {
                        return ServerResponse.badRequest()
                                .bodyValue("Invalid price format: " + priceStr);
                    }
                })
                .orElseGet(() -> ServerResponse.badRequest()
                        .bodyValue("Missing 'cheaperThan' parameter"));
    }

    public Mono<ServerResponse> getProductById(ServerRequest request) {
        String id = request.pathVariable("id");
        return productService.getProductById(id)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> streamProducts(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(productService.getProductUpdates(), Product.class);
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        return request.bodyToMono(Product.class)
                .flatMap(product -> {
                    if (product.getName() == null || product.getName().isEmpty()) {
                        return ServerResponse.badRequest()
                                .bodyValue("Product name cannot be empty");
                    }

                    if (product.getPrice() < 0) {
                        return ServerResponse.badRequest()
                                .bodyValue("Product price cannot be negative");
                    }

                    return productService.createProduct(product)
                            .flatMap(savedProduct ->
                                    ServerResponse.created(URI.create("/api/functional/products/" + savedProduct.getId()))
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(savedProduct));
                })
                .onErrorResume(e -> ServerResponse.badRequest()
                        .bodyValue("Invalid product data: " + e.getMessage()));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(Product.class)
                .flatMap(product -> productService.updateProduct(id, product)
                        .flatMap(updatedProduct ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(updatedProduct))
                        .switchIfEmpty(ServerResponse.notFound().build()))
                .onErrorResume(e -> ServerResponse.badRequest()
                        .bodyValue("Invalid product data: " + e.getMessage()));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        return productService.getProductById(id)
                .flatMap(product -> productService.deleteProduct(id)
                        .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
