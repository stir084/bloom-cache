package com.example.productcacheservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductCacheServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductCacheServiceApplication.class, args);
    }
}
