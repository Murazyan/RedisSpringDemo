package com.example.redisspringdemo.service;

import com.example.redisspringdemo.api.RequestDescriptor;
import com.example.redisspringdemo.configuration.ratelimit.RateLimitRule;
import com.example.redisspringdemo.util.RuleUtil;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.params.GetExParams;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;
@Component
public class JedisRateLimitService implements RateLimitService{

    private final Set<RateLimitRule> rateLimitRules;
    private final JedisCluster jedisCluster;
    private final String REDIS_KEY_PREFIX = "ratelimit:";

    private final RuleUtil util;

    public JedisRateLimitService(Set<RateLimitRule> rateLimitRules,
                                 JedisCluster jedisCluster,
                                 RuleUtil util) {
        this.rateLimitRules = rateLimitRules;
        this.jedisCluster= jedisCluster;
        this.util = util;
    }

    @Override
    public boolean shouldLimit(Set<RequestDescriptor> requestDescriptors) {
        Jedis jedis = new Jedis();

        for (RequestDescriptor descriptor : requestDescriptors) {
            String redisKey = REDIS_KEY_PREFIX + generateRedisKey(descriptor);
            List<RateLimitRule> matchesRules = util.findAllMatchedRules(rateLimitRules, descriptor);
            if (!matchesRules.isEmpty()) {
                String currentRequestsStr = jedis.get(redisKey);
                Integer currentRequests = currentRequestsStr != null ? Integer.parseInt(currentRequestsStr) - 1 : 1;
                Optional<RateLimitRule> first = matchesRules.stream().findFirst();
                if (first.isPresent()) {
                    final RateLimitRule rateLimitRule = first.get();
                    final TimeUnit timeUnit = TimeUnit.valueOf(rateLimitRule.getTimeInterval().name());
                    if (currentRequestsStr == null && rateLimitRule.getAllowedNumberOfRequests() > 0) {
                        jedis.set(redisKey, "" + (rateLimitRule.getAllowedNumberOfRequests() - 1));
                        jedis.expire(redisKey, 60);
                    } else if (currentRequests < 0 || rateLimitRule.getAllowedNumberOfRequests() <= 0) {
                        return false;
                    } else {
                        jedis.setex(redisKey, Long.parseLong(jedis.getEx(redisKey,GetExParams.getExParams())), "" + (Integer.parseInt(jedis.get(redisKey)) - 1));
                    }
                }
            }
        }
        return true;
    }

    // Generate a unique key based on the descriptor fields,
    // using accountId, clientIp, and requestType to create a key
    public String generateRedisKey(RequestDescriptor descriptor) {
        String KEY = "";
        KEY += descriptor.getAccountId() != null ? descriptor.getAccountId() + ":" : ":";
        KEY += descriptor.getClientIp() != null ? descriptor.getClientIp() + ":" : ":";
        KEY += descriptor.getRequestType() != null ? descriptor.getRequestType() : "";
        return KEY;
    }
}
