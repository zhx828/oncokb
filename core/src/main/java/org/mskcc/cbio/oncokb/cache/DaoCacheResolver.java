package org.mskcc.cbio.oncokb.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.ArrayList;
import java.util.Collection;

import static org.mskcc.cbio.oncokb.cache.Constants.DELIMITER;

public class DaoCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;

    public DaoCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<Cache> caches = new ArrayList<>();
        CacheKey nameKey = CacheKey.getByKey(CacheKeyType.DAO.name() + DELIMITER + context.getMethod().getName());
        if (nameKey != null) {
            caches.add(cacheManager.getCache(nameKey.getKey()));
        }
        return caches;
    }
}
