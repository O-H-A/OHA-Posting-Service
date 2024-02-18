package com.oha.posting.service;

import com.oha.posting.dto.external.ExternalUser;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class CacheService {

    private final Cache<Long, ExternalUser> userCache;

    public CacheService() {
        CacheManager cacheManager = org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder().build(true);

        ExpiryPolicy<Object, Object> expiryPolicy = ExpiryPolicyBuilder
                .timeToLiveExpiration(Duration.ofSeconds(600));

        CacheConfiguration<Long, ExternalUser> cacheConfig = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Long.class, ExternalUser.class, ResourcePoolsBuilder.heap(1000))
                .withExpiry(expiryPolicy)
                .build();

        userCache = cacheManager.createCache("user-cache", cacheConfig);
    }

    public void getUserCacheInfo(Set<Long> userIds, Set<Long> notCachedUserIds, List<ExternalUser> cachedUserList) {
        notCachedUserIds.clear();
        cachedUserList.clear();

        userIds.forEach(userId -> {
            if (userCache.get(userId) == null) {
                notCachedUserIds.add(userId);
            } else {
                cachedUserList.add(userCache.get(userId));
            }
        });
    }

    public void insertUserCache(List<ExternalUser> userList) {
        userList.forEach(user -> userCache.put(user.getUserId(), user));
    }
}