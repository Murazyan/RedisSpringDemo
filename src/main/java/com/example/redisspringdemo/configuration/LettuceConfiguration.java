package com.example.redisspringdemo.configuration;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LettuceConfiguration {

    @Bean
    //https://lettuce.io/core/release/reference/#_connection_pooling
    public StatefulRedisClusterConnection<String,String> lettuceRedisClusterConnection(){
        RedisClusterClient redisClient = RedisClusterClient.create("redis://redis-master-0:3000");
        StatefulRedisClusterConnection<String, String> connection = redisClient.connect();
        return connection;
    }

}
