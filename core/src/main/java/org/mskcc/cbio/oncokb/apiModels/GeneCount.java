package org.mskcc.cbio.oncokb.apiModels;

public class GeneCount {
    String entrezGeneId;
    Integer count;


    public GeneCount(String entrezGeneId, Integer count) {
        this.entrezGeneId = entrezGeneId;
        this.count = count;
    }

    public String getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(String entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
