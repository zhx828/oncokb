package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DaoCache {
    @Cacheable(cacheResolver = "daoCacheResolver", key = "'all'")
    public List<TumorType> getAllCancerTypes() {
        return ApplicationContextSingleton.getTumorTypeBo().findAll();
    }
}
