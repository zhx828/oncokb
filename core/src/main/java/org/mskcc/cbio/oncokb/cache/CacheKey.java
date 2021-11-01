package org.mskcc.cbio.oncokb.cache;


public enum CacheKey {
    CANCER_GENE_LIST("general:getCancerGenes"),
    CANCER_GENE_LIST_TXT("general:getCancerGenesTxt"),
    CURATED_GENE_LIST("general:getCuratedGenes"),
    CURATED_GENE_LIST_TXT("general:getCuratedGenesTxt"),
    FIND_GENE_BY_SYMBOL("general:findGeneBySymbol"),
    PROCESS_QUERY("general:processQuery"),
    GET_ALTERATION_FROM_GN("general:getAlterationFromGenomeNexus"),
    GET_ALL_FDA_ALTERATIONS("general:getAllFdaAlterations"),
    ONCOKB_INFO("general:getOncoKBInfo"),
    DAO_EVIDENCES_BY_GENE("dao:get")
    ;

    String key;

    CacheKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static CacheKey getByKey(String key) {
        for (CacheKey cacheKey : CacheKey.values()) {
            if (cacheKey.key.equalsIgnoreCase(key)) {
                return cacheKey;
            }
        }
        return null;
    }
}
