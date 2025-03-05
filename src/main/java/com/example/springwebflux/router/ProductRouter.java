package com.example.springwebflux.router;

import com.example.springwebflux.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
                .GET("/api/functional/products", handler::getAllProducts)
                .GET("/api/functional/products/sorted", handler::getSortedProducts)
                .GET("/api/functional/products/cheaper", handler::getCheaperProducts)
                .GET("/api/functional/products/events",
                        accept(MediaType.TEXT_EVENT_STREAM), handler::streamProducts)
                .GET("/api/functional/products/{id}", handler::getProductById)
                .POST("/api/functional/products", handler::createProduct)
                .PUT("/api/functional/products/{id}", handler::updateProduct)
                .DELETE("/api/functional/products/{id}", handler::deleteProduct)
                .build();
    }
}
