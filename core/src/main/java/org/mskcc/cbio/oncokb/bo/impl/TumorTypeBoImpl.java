package org.mskcc.cbio.oncokb.bo.impl;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.bo.TumorTypeBo;
import org.mskcc.cbio.oncokb.cache.DaoCache;
import org.mskcc.cbio.oncokb.dao.TumorTypeDao;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class TumorTypeBoImpl extends GenericBoImpl<TumorType, TumorTypeDao> implements TumorTypeBo {

    @Autowired
    DaoCache daoCache;

    @Override
    public TumorType findTumorTypeByCode(String code) {
        return this.daoCache.getAllCancerTypes().stream().filter(cancerType -> !StringUtils.isNullOrEmpty(cancerType.getCode()) && cancerType.getCode().equals(code)).findFirst().orElse(null);
    }

    public List<TumorType> findAllCached() {
        return this.daoCache.getAllCancerTypes();
    }

    public Set<TumorType> findAllSpecialCancerTypesCached() {
        List<TumorType> cancerTypes = this.daoCache.getAllCancerTypes();
        return Arrays.stream(SpecialTumorType.values()).map(specialTumorType -> cancerTypes.stream().filter(cancerType -> !StringUtils.isNullOrEmpty(cancerType.getMainType()) && cancerType.getMainType().equals(specialTumorType.getTumorType())).findAny().orElse(null)).filter(cancerType -> cancerType != null).collect(Collectors.toSet());
    }
}
