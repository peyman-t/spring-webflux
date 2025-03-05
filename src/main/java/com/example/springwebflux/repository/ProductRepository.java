package com.example.springwebflux.repository;

import com.example.springwebflux.model.Product;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProductRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    // Initialize with some sample data
    public ProductRepository() {
        addSampleProducts();
    }

    private void addSampleProducts() {
        Product p1 = new Product("1", "Laptop", 1299.99);
        Product p2 = new Product("2", "Smartphone", 799.99);
        Product p3 = new Product("3", "Headphones", 199.99);
        Product p4 = new Product("4", "Keyboard", 99.99);
        Product p5 = new Product("5", "Mouse", 49.99);

        products.put(p1.getId(), p1);
        products.put(p2.getId(), p2);
        products.put(p3.getId(), p3);
        products.put(p4.getId(), p4);
        products.put(p5.getId(), p5);
    }

    public Flux<Product> findAll() {
        return Flux.fromIterable(products.values());
    }

    public Mono<Product> findById(String id) {
        return Mono.justOrEmpty(products.get(id));
    }

    public Mono<Product> save(Product product) {
        products.put(product.getId(), product);
        return Mono.just(product);
    }

    public Mono<Void> deleteById(String id) {
        products.remove(id);
        return Mono.empty();
    }
}
