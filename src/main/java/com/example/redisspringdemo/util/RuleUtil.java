package com.example.redisspringdemo.util;


import com.example.redisspringdemo.api.RequestDescriptor;
import com.example.redisspringdemo.configuration.ratelimit.RateLimitRule;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RuleUtil {

    public List<RateLimitRule> findAllMatchedRules(final Set<RateLimitRule> rateLimitRules,
                                                   final RequestDescriptor descriptor) {
        return rateLimitRules.stream().filter(rl ->
                (rl.getAccountId().isPresent() && Strings.isEmpty(rl.getAccountId().get())) || (descriptor.getAccountId()!=null && Objects.equals(rl.getAccountId(), descriptor.getAccountId())) ||
                        (rl.getClientIp().isPresent() && Strings.isEmpty(rl.getClientIp().get())) || (descriptor.getClientIp()!=null && Objects.equals(rl.getClientIp(), descriptor.getClientIp())) ||
                        (rl.getRequestType().isPresent() && Strings.isEmpty(rl.getRequestType().get())) || (descriptor.getRequestType()!=null && Objects.equals(rl.getRequestType(), descriptor.getRequestType()))).collect(Collectors.toList());


    }
}
