package com.example.productcacheservice.service;

import com.example.productcacheservice.domain.Product;
import com.example.productcacheservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RBloomFilter<Long> productBloomFilter;

    private static final String CACHE_KEY_PREFIX = "product:";
    private static final long CACHE_TTL = 24; // 24시간

    public Product save(Product product) {
        Product savedProduct = productRepository.save(product);
        String cacheKey = CACHE_KEY_PREFIX + savedProduct.getId();
        redisTemplate.opsForValue().set(cacheKey, savedProduct, CACHE_TTL, TimeUnit.HOURS);
        productBloomFilter.add(savedProduct.getId());
        return savedProduct;
    }

    public Optional<Product> findById(Long id) {
        // 1. Bloom Filter 체크
        if (!productBloomFilter.contains(id)) {
            log.info("Product with id {} does not exist in Bloom Filter", id);
            return Optional.empty();
        }

        // 2. Redis Cache 체크
        String cacheKey = CACHE_KEY_PREFIX + id;
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            log.info("Product with id {} found in Redis cache", id);
            return Optional.of(cachedProduct);
        }

        // 3. Database 조회
        Optional<Product> productOptional = productRepository.findById(id);
        productOptional.ifPresent(product -> {
            redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL, TimeUnit.HOURS);
            productBloomFilter.add(id);
        });

        return productOptional;
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
        // Note: Bloom Filter는 삭제 연산을 지원하지 않습니다.
    }
}
