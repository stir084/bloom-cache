package com.example.productcacheservice;

import com.example.productcacheservice.domain.Product;
import com.example.productcacheservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BloomFilterTest {

    @Autowired
    private ProductService productService;

    @Test
    public void testFalsePositive() {
        // 1. 먼저 100개의 상품을 생성합니다
        for (long i = 1; i <= 100; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(1000.0);
            product.setDescription("Test Product " + i);
            productService.save(product);
        }

        // 2. 모든 상품을 삭제합니다
        for (long i = 1; i <= 100; i++) {
            productService.delete(i);
        }

        // 3. 삭제된 상품들을 조회해봅니다
        int falsePositives = 0;
        for (long i = 1; i <= 100; i++) {
            if (productService.findById(i).isPresent()) {
                falsePositives++;
                System.out.println("False positive detected for id: " + i);
            }
        }

        // 4. 결과 출력
        System.out.println("Total false positives: " + falsePositives);
        System.out.println("False positive rate: " + (falsePositives * 100.0 / 100) + "%");
    }
}
