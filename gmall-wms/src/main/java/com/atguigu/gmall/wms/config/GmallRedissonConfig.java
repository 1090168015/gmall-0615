package com.atguigu.gmall.wms.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GmallRedissonConfig {
    @Bean
    public RedissonClient redissonClient(){//RedissonClient用于分布式锁的处理
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.20.100:6379");
        return Redisson.create(config);
    }
}
