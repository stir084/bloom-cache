package com.example.productcacheservice.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Bean
    public RBloomFilter<Long> productBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("product-bloom-filter");
        // 더 작은 예상 항목 수와 더 높은 오탐율 설정
        bloomFilter.tryInit(100L, 0.1); // 100개의 항목, 10%의 오탐율
        return bloomFilter;
    }
}
