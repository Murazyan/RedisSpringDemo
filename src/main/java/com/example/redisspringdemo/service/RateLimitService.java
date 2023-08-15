package com.example.redisspringdemo.service;


import com.example.redisspringdemo.api.RequestDescriptor;

import java.util.Set;

public interface RateLimitService {

    default boolean shouldLimit(Set<RequestDescriptor> requestDescriptors) {
        return false;
    }

}
