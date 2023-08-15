package com.example.redisspringdemo;


import com.example.redisspringdemo.api.RequestDescriptor;
import com.example.redisspringdemo.configuration.ratelimit.RateLimitRule;
import com.example.redisspringdemo.configuration.ratelimit.RateLimitTimeInterval;
import com.example.redisspringdemo.service.JedisRateLimitService;
import com.example.redisspringdemo.util.RuleUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.testcontainers.shaded.com.google.common.collect.Sets;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JedisRateLimitServiceTest {

//    @Mock
//    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;


    @Mock
    private RuleUtil util;

    @InjectMocks
    private JedisRateLimitService rateLimitService;

    @Before
    public void setup() {
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testAllowRequest() {
        // Prepare test data
        RequestDescriptor descriptor = new RequestDescriptor(Optional.of("accountId1"),
                Optional.of("clientIp1"), Optional.of("requestType1"));
        Set<RequestDescriptor> descriptors = Sets.newHashSet(descriptor);

        RateLimitRule rule = new RateLimitRule();
        rule.setAccountId(Optional.of("accountId1"));
        rule.setAllowedNumberOfRequests(2);
        rule.setTimeInterval(RateLimitTimeInterval.MINUTES);

        when( util.findAllMatchedRules(anySet(), any())).thenReturn(Arrays.asList(rule));

        boolean result = rateLimitService.shouldLimit(descriptors);

        assertTrue(result);

    }

    @Test
    public void testAllowRequest_ExceedLimit() {
        Set<RequestDescriptor> descriptors = new HashSet<>();
        descriptors.add(new RequestDescriptor(Optional.of("1"), Optional.of("127.0.0.1"), Optional.of("login")));

        RateLimitRule rule = new RateLimitRule();
        rule.setAccountId(Optional.of("1"));
        rule.setAllowedNumberOfRequests(0);
        rule.setTimeInterval(RateLimitTimeInterval.MINUTES);
        when( util.findAllMatchedRules(anySet(), any())).thenReturn(Arrays.asList(rule));

        boolean result = rateLimitService.shouldLimit(descriptors);

        assertTrue(result);
    }
}